package ru.yandex.practicum.filmorate.model;

public enum MpaRating {

    G(0, "У фильма нет возрастных ограничений."),
    PG(0, "Детям рекомендуется смотреть фильм с родителями."),
    PG_13(13, "Детям до 13 лет просмотр не желателен."),
    R(17, "Лицам до 17 лет просматривать фильм можно только в присутствии взрослого."),
    NC_17(18, "Лицам до 18 лет просмотр запрещён.");

    private final int minAge;
    private final String description;

    MpaRating(int minAge, String description) {
        this.minAge = minAge;
        this.description = description;
    }

    public int getMinAge() {
        return minAge;
    }

    public String getDescription() {
        return description;
    }
}