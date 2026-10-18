# Oran Info

تطبيق Android فيه غير الفيديوهات تاعك. قبل كل فيديو تطلع إعلانة AdMob.
الفيديوهات ما تدخلش في حجم التطبيق: التطبيق يجيبهم من الأنترنت.

## الطريقة بلا Android Studio (GitHub)

1. دير حساب في github.com وكريي Repository جديد (Public)، سميه مثلا `videoapp`.
2. ارفع كل محتوى هذا المجلد (Add file > Upload files).
3. ارفع فيديوهاتك (01.mp4, 02.mp4...) في مجلد `videos`.
4. عدّل `videos/list.txt`: كل سطر رابط فيديو، وبدل `USERNAME` و`REPO` باسمك واسم الـ repo.
5. عدّل `app/src/main/res/values/strings.xml`: بدل `USERNAME` و`REPO` في `videos_list_url`.
6. روح لتبويب **Actions** > **Build APK** > **Run workflow**. بعد دقايق، حل التشغيلة وحمل `OranInfo` من Artifacts.
7. فك الزيب اللي نزل، وثبت `OranInfo-debug.apk` في تيليفونك.

زيادة فيديو جديد = ترفعو في `videos` وتزيد سطر في `list.txt`، بلا ما تعاود تبني التطبيق.

## AdMob

- فيه IDs تاع **التجربة** من Google، ما تجيبلكش دراهم.
- قبل النشر بدل `admob_app_id` و`admob_interstitial_id` في `strings.xml` بـ IDs تاع حسابك في AdMob.
- `showAdEveryNVideos` في `MainActivity.kt` يحدد كل كم فيديو تطلع إعلانة.
