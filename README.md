# WebView Builder

Минимальное Android-приложение для открытия HTML-файлов и папок с проектами в WebView.

## Что умеет
- открыть один HTML-файл
- выбрать папку проекта
- найти `index.html` внутри папки
- открыть результат в WebView
- поддерживать статический HTML/CSS/JS проект без сервера

## Важно
Это проект только для WebView / статических сайтов.
Для Vite, Node.js, npm и APK сборки нужен отдельный build environment.

## Как использовать
1. Откройте проект в Android Studio.
2. Убедитесь, что Android SDK установлен.
3. Запустите приложение на устройстве или эмуляторе.
4. Нажмите:
   - `Выбрать HTML-файл` — для одного файла
   - `Выбрать папку проекта` — для папки с `index.html`, CSS, JS и т.д.
5. Проект откроется в WebView.

## Структура
- `app/src/main/java/com/example/webviewbuilder/MainActivity.kt` — логика WebView
- `app/src/main/res/layout/activity_main.xml` — экран приложения
- `app/src/main/AndroidManifest.xml` — манифест
