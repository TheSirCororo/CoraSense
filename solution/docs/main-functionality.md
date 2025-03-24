# Основной функционал

Документация по эндпоинтам API функционала доступна здесь: http://localhost:8080/swagger. 

Если необходимо получить доступ без запуска приложения, то тыкните [сюда](../openapi/api.json).

## Клиенты и рекламодатели
На Земле есть два типа людей - клиенты и рекламодатели. Клиенты смотрят рекламу, рекламодатели её придумывают и размещают
на нашей прекрасной платформе. И для тех, и для тех мы реализовываем API.

Ниже приведена демонстрация работы эндпоинтов, связанных с созданием/редактированием клиентов/рекламодателей.

<details>
<summary>Создание/сохранение клиентов</summary>
<img src="../static/functionality/clients-bulk.png" alt="Clients bulk">
</details>

<details>
<summary>Получение клиента</summary>
<img src="../static/functionality/clients-get.png" alt="Clients get">
</details>

<details>
<summary>Создание/сохранение рекламодателей</summary>
<img src="../static/functionality/advertisers-bulk.png" alt="Advertisers bulk">
</details>

<details>
<summary>Получение рекламодателя</summary>
<img src="../static/functionality/advertisers-get.png" alt="Advertisers get">
</details>

## Рекламные объявления
Объект преткновения всего приложения - рекламные объявления. Мы позволяем создавать, получать, редактировать и удалять их.

<details>
<summary>Создание рекламного объявления</summary>
<img src="../static/functionality/campaign-post.png" alt="Campaign post">
</details>

<details>
<summary>Получение рекламного объявления</summary>
<img src="../static/functionality/campaign-get.png" alt="Campaign get">
</details>

<details>
<summary>Получение рекламных объявлений рекламодателя</summary>
<img src="../static/functionality/advertisers-campaigns-get.png" alt="Advertisers campaigns get">
</details>

<details>
<summary>Изменение рекламного объявления</summary>
<img src="../static/functionality/campaign-put.png" alt="Campaign put">
</details>

<details>
<summary>Удаление рекламного объявления</summary>
<img src="../static/functionality/campaign-delete.png" alt="Campaign delete">
</details>

## Интересы клиентов
Не все клиенты одинаковые человеки, мы даём возможность учитывать их интересы двумя путями:
1. Таргетинг рекламы
2. Крутая ML, которая умеет определять интересы людей

<details>
<summary>Установка таргетинга в рекламе и у клиента</summary>
<img src="../static/functionality/targeting-set.png" alt="Targeting set">
</details>

<details>
<summary>Установка ML скора</summary>
<img src="../static/functionality/ml-score.png" alt="Targeting set">
</details>

## И... показ!
Ради чего мы все тут собрались? Конечно же, чтобы показывать рекламу. Собственно, вот ожидаемый функционал.

Алгоритм работает следующим образом:
1. "Цена" рекламы высчитывается так: `cost_per_impression + CLICK_WEIGHT * cost_per_click`
2. Высчитывается максимальная и минимальная "цена", максимальный и минимальный ML скор за каждую рекламу (для тех, что подходят пользователю по таргетингу и лимитам)
3. Высчитывается нормализация "цены" и ML скора для каждой рекламы следующим образом: `(x - x_min) / (x_max - x_min)`. Нормализация умножается на коэффициенты,
заданные искусственным образом для "цены" и ML скора (веса). 
4. Высчитываются очки лимитов (чтобы наиболее полным образом заполнять лимиты, заданные рекламодателем): `1 - (impressions_count / impressions_limit)` и умножаются на коэффициент (вес)
5. Все значения складываются: `cost_score + ml_score + limit_score` = `campaign_score`.
6. Кампании сортируются по возрастанию `user_seen` (видел ли пользователь рекламу), по убыванию `campaign_score`, по убыванию "цены"
7. Берётся первая реклама из ответа

<details>
<summary>Показ рекламного объявления пользователю</summary>
<img src="../static/functionality/get-ads.png" alt="Get ads">
</details>

<details>
<summary>Фиксация клика по рекламе</summary>
<img src="../static/functionality/click-ad.png" alt="Click ad">
</details>

## Статистика
Конечно же, рекламодателям интересен ход их рекламной кампании. Поэтому вот демонстрация работы статистики:

<details>
<summary>Общая статистика рекламодателя</summary>
<img src="../static/functionality/advertiser-total-stats.png" alt="Advertiser total stats">
</details>

<details>
<summary>Ежедневная статистика рекламодателя</summary>
<img src="../static/functionality/advertiser-daily-stats.png" alt="Advertiser daily stats">
</details>

<details>
<summary>Общая статистика рекламной кампании</summary>
<img src="../static/functionality/campaign-total-stats.png" alt="Campaign total stats">
</details>

<details>
<summary>Ежедневная статистика рекламной кампании</summary>
<img src="../static/functionality/campaign-daily-stats.png" alt="Campaign daily stats">
</details>

## Машина времени
Наши инженеры изобрели машину времени, но сломали естественный ход времени. Поэтому теперь мы как Майкл Джей Фокс.

<details>
<summary>Машина времени</summary>
<img src="../static/functionality/time-advance.png" alt="Time advance">
</details>

## Изображения в рекламных объявлениях
Картинки гораздо лучше привлекают человеческое внимание. Поэтому мы добавили возможность встраивать в объявления изображения.

Имеется 2 режима хранения картинок:
1. `FILE` - хранение картинок в локальной файловой системе. Для сохранения между перезапусками контейнера используется docker volume.
2. `S3` - удалённое S3 хранилище. Необходимо указать S3 ключ и S3 бакет.

Смотрите секцию [переменных среды](build-and-run.md) для настройки этого фукнционала.

Изображения хранятся в базе данных в паре id(uuid) - название_файла. В рекламные кампании их можно передать POST/PUT запросом в виде id.

Изображения можно удалять с хранилища и БД DELETE запросом. Но тогда нужно убедиться, что оно ни в какой кампании не используется.

При получении рекламы пользователь получает image_id. Он может его использовать, чтобы скачать/загрузить изображение
(в зависимости от параметра download, подробнее см. в OpenAPI спецификации).

<details>
<summary>Загрузка изображения</summary>
<img src="../static/functionality/images-upload.png" alt="Image upload">
</details>

<details>
<summary>Скачивание изображения</summary>
<img src="../static/functionality/images-get.png" alt="Image get">
</details>

<details>
<summary>Удаление изображения</summary>
<img src="../static/functionality/images-delete.png" alt="Image delete">
</details>

<details>
<summary>Установка изображения в рекламное объявление</summary>
<img src="../static/functionality/image-set-to-campaign.png" alt="Image set to campaign">
</details>

## Модерация
Не все рекламодатели и клиенты доброжелательны. Так что в приложении есть модерация в следующих контекстах:
1. Логин клиента
2. Имя рекламодателя
3. Заголовок рекламы
4. Текст рекламы

Существует два режима модерации:
1. `LLM` - модерация при помощи LLM
2. `BLACKLIST` - модерация при помощи задаваемого чёрного списка слов (см. в OpenAPI)

Модерация в приложении реализована крайне гибко. Необходимо задать общий режим модерации, а отдельно для
каждого контекста можно указать свой режим модерации. При необходимости можно исключить контекст из модерации вообще.
По умолчанию применены следующие настройки:
1. Логин клиента модерируется чёрным списком
2. Имя рекламодателя модерируется чёрным списком
3. Заголовок и текст рекламы модерируются так, как задано общим режимом модерации (по умолчанию - LLM).

<details>
<summary>Отклонение текста из-за непрохождения модерации</summary>
<img src="../static/functionality/moderation-campaign.png" alt="Moderation (campaign)">
</details>

<details>
<summary>Связанные строчки в конфигурации</summary>
<img src="../static/functionality/moderation-config.png" alt="Moderation config">
</details>

### Управление модерацией
1. Для настройки модерации при запущенном приложении реализованы специальные endpoint'ы. Подробнее в OpenAPI спецификации.
Этот способ подойдёт, только если настройки необходимо изменить оперативно без перезагрузки приложения.
2. Кроме того, модерацию можно настраивать через переменные среды. Подробнее в [другой секции](build-and-run.md).
3. Модерацию можно включать/выключать. Есть специальная переменная среды, кроме того, есть endpoint'ы.

P.S. Контексты модерации и их режимы настраиваются только двумя путями: прямым изменением конфигурации [здесь](../src/main/resources/application.conf),
либо эндпоинтами. Общий режим можно настроить при помощи переменной среды.

<details>
<summary>Скриншот из Swagger UI со связанными эндпоинтами</summary>
<img src="../static/functionality/moderation-configure-swagger.png" alt="Moderation configure swagger">
</details>

## Генерация текста при помощи LLM
Некоторые рекламодатели доверяют написание текста рекламы LLM. Мы дадим им эту возможность.

Мы реализовали эндпоинт, который выдаёт тексты реклам по запросу, нужно лишь передать ID рекламодателя и предполагаемый заголовок.

Требует включения LLM.

<details>
<summary>Генерация текста</summary>
<img src="../static/functionality/llm-generate.png" alt="LLM generate">
</details>

## Визуализация статистики
Какие-то странные JSON'ы трудно понять людям, сидящим в своём кресле на последних этажах офисного центра
и попивающим кофе. Для них мы визуализировали статистику при помощи Grafana и Prometheus. Немного подробнее [здесь](foreign-services.md).

## Telegram бот
Чтобы рекламодателям стало совсем удобно, мы реализовали телеграм бота. Подробнее [тут](telegram.md)