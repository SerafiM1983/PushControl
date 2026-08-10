package com.serafimApp.pushcontrol.DataBaze;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static com.serafimApp.pushcontrol.Constans.PreferencesConstants.*;

import com.serafimApp.pushcontrol.LogCat;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
	LogCat logCat = new LogCat(false); // Отключен лог
	// Настройки базы данных
	private static final String DATABASE_NAME = "notifications.db";
	private static final int DATABASE_VERSION = 1;
	private final Context context;

	// Названия таблицы и столбцов
	public static final String TABLE_NAME = "captured_notifications";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_PACKAGE = "package_name";
	public static final String COLUMN_TITLE = "title";
	public static final String COLUMN_TEXT = "text";
	public static final String COLUMN_TIMESTAMP = "timestamp";
	public static final String COLUMN_IMAGE = "notification_image";
	public static final String COLUMN_SERVER_ID = "server_id";

	// SQL-запрос для создания таблицы
	private static final String TABLE_CREATE =
			"CREATE TABLE " + TABLE_NAME + " (" +
					COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
					COLUMN_PACKAGE + " TEXT, " +
					COLUMN_SERVER_ID + " TEXT," +
					COLUMN_TITLE + " TEXT, " +
					COLUMN_TEXT + " TEXT, " +
					COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
					COLUMN_IMAGE + " BLOB, " +
					// Умная уникальность: защищает и от повторов системных пушей, и от дублей внутри чатов
					"UNIQUE(" + COLUMN_PACKAGE + ", " + COLUMN_SERVER_ID + ", " + COLUMN_TEXT + ")" +
					");";

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(TABLE_CREATE);
		// Создаем индекс для быстрого поиска
		db.execSQL("CREATE INDEX IF NOT EXISTS idx_package ON " + TABLE_NAME + " (" + COLUMN_PACKAGE + ");");
		logCat.logShow(DATABASE_HELPER, "Таблица базы данных успешно созданна");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Проверяем: если старая версия была 1, а новая стала 2 (или выше)
		if (oldVersion < 2) {
			try {
				// Безопасно добавляем новую колонку COLUMN_IMAGE с типом BLOB
				// У старых записей в этой колонке автоматически появится значение NULL
				db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_IMAGE + " BLOB;");
			} catch (Exception e) {
				// Если что-то пошло не так, пишем в лог, чтобы приложение не упало
				logCat.logShow(DATABASE_HELPER,  "Ошибка при добавлении колонки картинок: " + e.getMessage());
			}
		}

		// Если в будущем (в версии 3, 4 и т.д.) потребуется добавить ещё колонки,
		// вы просто допишете сюда новые блоки if (oldVersion < 3) и т.д.
	}


	/**
	 * Метод для сохранения уведомления в базу данных
	 */
	public long insertNotification(NotificBD push) {
		// Открываем базу данных для записи
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();

		// Заполняем поля данными
		values.put(COLUMN_PACKAGE, push.getPackageName());
		values.put(COLUMN_SERVER_ID, push.getServerId()); // Записываем серверный ID
		values.put(COLUMN_TITLE, push.getTitle());
		values.put(COLUMN_TEXT, push.getText());
		values.put(COLUMN_IMAGE,push.getImage());

		// Вставляем строку в таблицу
		long newRowId = db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
		if (newRowId == -1) {
			logCat.logShow(DATABASE_HELPER,  "Ошибка при сохранении уведомления в SQLite");
		} else {
			logCat.logShow(DATABASE_HELPER,  "Уведомление успешно сохранено в SQLite. ID строки: " + newRowId);
		}
		return newRowId;
	}

	public void logAllNotifications() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, COLUMN_ID + " DESC");

		if (cursor.moveToFirst()) {
			do {
				int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
				String pkg = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PACKAGE));
				String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
				String text = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEXT));
				String time = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP));
			} while (cursor.moveToNext());
		}
		cursor.close();
	}

	/**
	 * Метод для получения уведомлений конкретного приложения
	 */
	public List<NotificBD> getNotificationsByPackage(String packageName) {
		List<NotificBD> list = new ArrayList<>();
		SQLiteDatabase db = this.getReadableDatabase();

		// Запрос с условием WHERE по имени пакета. Сортировка от новых к старым.
		Cursor cursor = db.query(
				TABLE_NAME,
				null,
				COLUMN_PACKAGE + " = ?",
				new String[]{packageName},
				null, null,
				COLUMN_ID + " DESC"
		);

		if (cursor != null) {
			// КЭШИРУЕМ ИНДЕКСЫ КОЛОНОК ОДИН РАЗ ДО ЦИКЛА (Это сильно ускорит работу)
			int pkgIdx = cursor.getColumnIndexOrThrow(COLUMN_PACKAGE);
			int titleIdx = cursor.getColumnIndexOrThrow(COLUMN_TITLE);
			int textIdx = cursor.getColumnIndexOrThrow(COLUMN_TEXT);
			int idIdx = cursor.getColumnIndexOrThrow(COLUMN_ID);
			int imgIdx = cursor.getColumnIndexOrThrow(COLUMN_IMAGE);
			if (cursor.moveToFirst()) {
				do {
					String pkg = cursor.getString(pkgIdx);
					String title = cursor.getString(titleIdx);
					String text = cursor.getString(textIdx);
					Long id = cursor.getLong(idIdx);

					// Чтение байтов картинки
					byte[] image = cursor.getBlob(imgIdx);

					list.add(new NotificBD(pkg, text, title, image, id));
				} while (cursor.moveToNext());
			}
			cursor.close();
		}
		return list;
	}

	/**
	 * Метод для получения вообще всех сохраненных уведомлений
	 */
	public List<NotificBD> getAllNotifications() {
		List<NotificBD> list = new ArrayList<>();
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, COLUMN_ID + " DESC");

		if (cursor.moveToFirst()) {
			do {
				String pkg = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PACKAGE));
				String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
				String text = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEXT));
				Long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
				byte[] image = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_IMAGE));
				list.add(new NotificBD(pkg, text, title, image, id));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return list;
	}

	/**
	 * Метод для удаления
	 */
	public void deleteItem(long id) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
		SharedPreferences prefDel = context.getSharedPreferences(DELETE_COUNT_DB, Context.MODE_PRIVATE);
		int currentCount = prefDel.getInt(KEY_DELETE_COUNT, 0);
		currentCount++; // Увеличиваем на 1
		// 2. Проверяем, набралось ли 50 удалений
		if (currentCount >= 50) {
			// Набралось! Запускаем сжатие в фоновом потоке
			new Thread(() -> {
				try {
					db.execSQL("VACUUM");
				} catch (Exception e) {
					logCat.logShow(DATABASE_HELPER,  "Ошибка VACUUM = " + e);
				}
			}).start();
			// Сбрасываем счетчик в 0
			prefDel.edit().putInt(KEY_DELETE_COUNT, 0).apply();
		} else {
			// Еще не набралось — просто сохраняем новое число обратно в SharedPreferences
			prefDel.edit().putInt(KEY_DELETE_COUNT, currentCount).apply();
		}
	}

	// Удалить абсолютно все уведомления из таблицы
	public void clearAllNotifications() {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_NAME, null, null);
		db.close();
	}

	// Удалить уведомления только конкретного приложения
	public void clearNotificationsByPackage(String packageName) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_NAME,  COLUMN_PACKAGE + " = ?", new String[]{packageName});
		db.close();
	}
}
