package com.example.pushcontrol.DataBaze;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
	private static final String TAG = "DatabaseHelper";

	// Настройки базы данных
	private static final String DATABASE_NAME = "notifications.db";
	private static final int DATABASE_VERSION = 1;

	// Названия таблицы и столбцов
	public static final String TABLE_NAME = "captured_notifications";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_PACKAGE = "package_name";
	public static final String COLUMN_TITLE = "title";
	public static final String COLUMN_TEXT = "text";
	public static final String COLUMN_TIMESTAMP = "timestamp";

	// SQL-запрос для создания таблицы
	private static final String TABLE_CREATE =
			"CREATE TABLE " + TABLE_NAME + " (" +
					COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
					COLUMN_PACKAGE + " TEXT, " +
					COLUMN_TITLE + " TEXT, " +
					COLUMN_TEXT + " TEXT, " +
					COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP" +
					");";

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(TABLE_CREATE);
		Log.d(TAG, "Таблица базы данных успешно создана.");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(db);
	}

	/**
	 * Метод для сохранения уведомления в базу данных
	 */
	public void insertNotification(NotificBD push) {
		// Открываем базу данных для записи
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();

		// Заполняем поля данными
		values.put(COLUMN_PACKAGE, push.getPackageName());
		values.put(COLUMN_TITLE, push.getTitle());
		values.put(COLUMN_TEXT, push.getText());

		// Вставляем строку в таблицу
		long newRowId = db.insert(TABLE_NAME, null, values);

		if (newRowId == -1) {
			Log.e(TAG, "Ошибка при сохранении уведомления в SQLite");
		} else {
			Log.d(TAG, "Уведомление успешно сохранено в SQLite. ID строки: " + newRowId);
		}
	}

	public void logAllNotifications() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, COLUMN_ID + " DESC");

		Log.d(TAG, "=== ВСЕГО ЗАПИСЕЙ В БАЗЕ: " + cursor.getCount() + " ===");

		if (cursor.moveToFirst()) {
			do {
				int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
				String pkg = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PACKAGE));
				String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
				String text = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEXT));
				String time = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP));

				Log.d(TAG, "ID: " + id + " | " + time + " | Пакет: " + pkg + " | [" + title + "] " + text);
			} while (cursor.moveToNext());
		}
		cursor.close();
		Log.d(TAG, "=================================");
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

		if (cursor.moveToFirst()) {
			do {
				String pkg = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PACKAGE));
				String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
				String text = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEXT));

				// Предполагается, что у вашего класса NotificBD конструктор принимает: (package, text, title)
				// согласно вашему коду в NotificationCatcherService
				list.add(new NotificBD(pkg, text, title));
			} while (cursor.moveToNext());
		}
		cursor.close();
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

				list.add(new NotificBD(pkg, text, title));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return list;
	}


}
