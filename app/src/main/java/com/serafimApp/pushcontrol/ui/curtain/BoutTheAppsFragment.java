package com.serafimApp.pushcontrol.ui.curtain;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.serafimApp.pushcontrol.R;
import com.serafimApp.pushcontrol.databinding.BoutTheAppBinding;
import com.yandex.mobile.ads.common.AdRequestConfiguration;
import com.yandex.mobile.ads.common.AdRequestError;
import com.yandex.mobile.ads.common.ImpressionData;
import com.yandex.mobile.ads.interstitial.InterstitialAd;
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener;
import com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener;
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader;
import com.yandex.mobile.ads.common.AdError;
import static com.serafimApp.pushcontrol.Constans.PreferencesConstants.ID_2;



public class BoutTheAppsFragment extends Fragment {
	private BoutTheAppBinding binding;
	private InterstitialAd mInterstitialAd = null;
	private InterstitialAdLoader mInterstitialAdLoader = null;
	private final String AD_UNIT_ID = "demo-interstitial-yandex"; // Тестовый ID Яндекса


	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		binding = BoutTheAppBinding.inflate(inflater, container, false);
		View root = binding.getRoot();

		// 1. Инициализируем загрузчик для версии 7.12.0
		mInterstitialAdLoader = new InterstitialAdLoader(requireContext());

		// 2. Настраиваем слушатель загрузки ресурсов в кэш
		mInterstitialAdLoader.setAdLoadListener(new InterstitialAdLoadListener() {
			@Override
			public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
				mInterstitialAd = interstitialAd;
				// Как только реклама скачалась — вешаем на неё слушатель событий показа/закрытия
				setupAdCallbacks();
			}

			@Override
			public void onAdFailedToLoad(@NonNull AdRequestError adRequestError) {
				mInterstitialAd = null;
			}
		});

		// 3. Запускаем фоновую загрузку первого баннера
		loadInterstitialAd();

		// 4. Обработчик кнопки поддержки разработчика
		binding.btnSupport.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mInterstitialAd != null && getActivity() != null) {
					// Показываем рекламу на весь экран планшета
					mInterstitialAd.show(getActivity());
				} else {
					// Если интернета нет или баннер не успел скачаться
					Toast.makeText(getContext(), "Thank you for your support!", Toast.LENGTH_SHORT).show();
				}
			}
		});

		((androidx.appcompat.app.AppCompatActivity) requireActivity()).getSupportActionBar().setTitle(
				ContextCompat.getString(getContext(), R.string.bout_the_apps));

		return root;
	}

	/**
	 * Создание запроса по правилам версии 7.12.0
	 */
	private void loadInterstitialAd() {
		if (mInterstitialAdLoader != null) {
			AdRequestConfiguration adRequestConfiguration = new AdRequestConfiguration.Builder(ID_2)
					.build();
			mInterstitialAdLoader.loadAd(adRequestConfiguration);
		}
	}

	/**
	 * Слушатель жизненного цикла показанного баннера
	 */
	private void setupAdCallbacks() {
		if (mInterstitialAd == null) return;

		mInterstitialAd.setAdEventListener(new InterstitialAdEventListener() {
			@Override
			public void onAdShown() {
				// Реклама успешно развернулась на экране
			}

			@Override
			public void onAdFailedToShow(@NonNull AdError adError) {
				// ИСПРАВЛЕНО: Используем класс AdError вместо AdRequestError
				mInterstitialAd = null;
				loadInterstitialAd(); // Пробуем скачать новый баннер про запас
			}

			@Override
			public void onAdDismissed() {
				mInterstitialAd = null;
				// Пользователь закрыл рекламу крестиком
				loadInterstitialAd();
				Toast.makeText(getContext(), "Thank you for supporting us!", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onAdClicked() {
				// Пользователь перешел по рекламе
			}

			@Override
			public void onAdImpression(ImpressionData impressionData) {
				// Засчитан коммерческий показ
			}
		});
	}


	@Override
	public void onDestroyView() {
		// Обязательная очистка ссылок для предотвращения утечек памяти в версии 7.x
		if (mInterstitialAdLoader != null) {
			mInterstitialAdLoader.setAdLoadListener(null);
			mInterstitialAdLoader = null;
		}
		if (mInterstitialAd != null) {
			mInterstitialAd.setAdEventListener(null);
			mInterstitialAd = null;
		}
		super.onDestroyView();
		binding = null;
	}
}