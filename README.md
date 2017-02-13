# Topface для Android

![Topface для Android](https://lh5.ggpht.com/IeTGlZNc3b031FWAs609XIgtxrFd8YolpF3n2q5emStxTzcdPgl-1Tjx0I6oARWS4Q=w60)

## Доступ к коду
Если вы видите этот Readme, то у вас есть полный доступ ко всему исходному коду, но только на чтение.
Для того, что бы начать разработку следует форкнуть проект (обязательно от имени вашего пользователя, а не в сторонний проект). Для этого достаточно нажать кнопку [Fork] справа вверху.

После чего вы получаете полную версию репозитория и можете создавать в ней свои ветки для последующей разработки (вы можете изменять и другие ветки, например master или develop, но изменения из них не будут приняты во время pull mRequest).

## Слияние ваших изменений с основным кодом
После того как вы закончили разарботку и тестирование вашего функционала в отдельной ветки вам следует залить его в основной репозиторий Topface/topface-android.

Что бы это сделать нужно запушить вашу локальную ветку в вашу версию репозитория (если вы еще не сделали этого заранее) и после этого на GitHub переключиться на залитую ветку и нажать большую зеленую кнопку [Compare & pull mRequest]. После этого вас попросят указать название и описание (это обязательно нужно сделать, чем подробнее, тем лучше) и, самое главное, ветку в которую вы хотите, что бы попал ваш код (это обязательно нужно согласовать, нельзя например просто так вмержится в master).

После отправки пул реквеста его проверит ваш мердж мастер, если все хорошо, он будет влит в основной код, если не хорошо, то он напишет комментарий к вашему реквесту.

Тикет не считается законченым, если он не залит в основной репозиторий (не важно в какую ветку).

## Загрузка изменений из основного репозитория
При разработке вашим основным репозиторием будет счатиться ваш форк, туда пушите только вы, но изменения основного репозитория автоматически туда не попадают.

Для того, что бы их обновить нужно добавить еще один remote сервер, URL которого соответсвует основному репозитория;

    git remote add upstream git@github.com:Topface/topface-android.git

После этого вы можете обновлять ваши локальные ветки напрямую с основного репозитория таким способом:

    git pull upstream ИМЯ_УДАЛЕННОЙ_ВЕТКИ_ДЛЯ_PULL_С_ТЕКУЩЕЙ


Если вы случайно склонировали основной репозиторий к себе и уже сделали изменения и теперь не можете запушить их, то можно одной командой переключить ваш локальный репозиторий на URL вашего удаленного:

    git remote set-url origin URL_ВАШЕЙ_КОПИИ_РЕПОЗИТОРИЯ

## Начало работы
Для начала разработки проекта нужно выполнитель следующие команды

    git checkout develop # Начинаем разработку с последней девелоперской версии
    git submodule init && git submodule update # Подтягиваем подмодули

## Сборка
Вся сборка проекта осуществляется через [Gradle](http://tools.android.com/tech-docs/new-build-system/user-guide)
Соответственно нужно его [устновить](http://www.gradle.org/downloads) и/или заиметь IDE с его нативной поддержкой

Для сборки через gradle должна быть устновлена переменная окружения ANDROID_HOME или свойство sdk.dir в файле local.properties (его нужно создать в корне проекта)
Для первоначальной сборки достаточно выполнить (будут собраны как debug версия, так и подписаный release) 

    gradle build

Возможно и установка сразу на устройство

    gradle installDebug #Собрать и установить debug версию

## Unit тесты
Тесты использующие локальную JVM, и которые не требуют запуска в runtime на девайсе
Лежат в папке test
Запуск:

AndroidStudio: переключаем в Build Variants->Test artifact на Unit Tests, запускаем через конфигурации или индивидуально через контекстное меню

Gradle
    
    gradle test<Тип сборки(необязательно)>

Создание и поддержка:
Т.к. Unit тесты запускаются на локальной JVM, они не имеют настоящего Android runtime, но все классы android.* доступны с помощью Robolectric
Отчеты по тестам находятся в build/reports директории для каждого типа сборки

## Ключ для подписи APK
Ключ для подписи apk файла (необходим для загрузки в Android Market), но проще собирать release через Gradle, там все конфиги сборки уже настроены: 
misc/topface-apk.key

    логин: topface
    пароль: Sonetica2012
