package com.topface.topface.requests.handlers;

/**
 * Error codes
 */
@SuppressWarnings("unused")
public class ErrorCodes {
    /**
     * Возможные ошибки запросов (если >= 0, то это серверная ошиба, если < 0, то это внутренняя ошибка
     * Подробнее: http://tasks.verumnets.ru/projects/topface/wiki/%D0%9A%D0%BE%D0%B4%D1%8B_%D0%BE%D1%88%D0%B8%D0%B1%D0%BE%D0%BA
     */

    public static final int NULL_RESPONSE = -7;
    public static final int WRONG_RESPONSE = -6;
    public static final int UNCONFIRMED_LOGIN_ACTION = -5;
    public static final int RESULT_DONT_SET = -4;
    public static final int CONNECTION_ERROR = -3;
    public static final int ERRORS_PROCCESED = -2;
    public static final int RESULT_OK = -1;

    public static final int INTERNAL_SERVER_ERROR = 0;

    public static final int UNKNOWN_SOCIAL_USER = 1;
    public static final int UNKNOWN_PLATFORM = 2;
    public static final int SESSION_NOT_FOUND = 3;
    public static final int UNSUPPORTED_CITIES_FILTER = 4;
    public static final int MISSING_REQUIRE_PARAMETER = 5;
    public static final int USER_NOT_FOUND = 6;
    public static final int UNSUPPORTED_LOCALE = 7;
    public static final int CANNOT_SENT_RATE = 8;
    public static final int MESSAGE_TOO_SHORT = 9;
    public static final int CANNOT_SENT_MESSAGE = 10;
    public static final int DETECT_FLOOD = 11;
    public static final int INCORRECT_PHOTO_URL = 12;
    public static final int DEFAULT_ERO_PHOTO = 13;
    public static final int PAYMENT = 14; // not enough coins
    public static final int INCORRECT_VOTE = 15;
    public static final int INVALID_TRANSACTION = 16;
    public static final int INVALID_PRODUCT = 17;
    public static final int INVERIFIED_RECEIPT = 18;
    public static final int ITUNES_CONNECTION = 19;
    public static final int UNVERIFIED_TOKEN = 20;
    public static final int INVALID_FORMAT = 21;
    public static final int UNVERIFIED_SIGNATURE = 22;
    public static final int INCORRECT_VALUE = 23;
    public static final int BANNER_NOT_FOUND = 24;
    public static final int NOTEQUAL_TRANSACTIONS = 25;
    public static final int CANNOT_SENT_GIFT = 26;
    public static final int MAINTENANCE = 27;
    public static final int BAN = 28;
    public static final int NETWORK_CONNECT_ERROR = 29;
    public static final int FEED_NOT_FOUND = 30;
    public static final int CANNOT_DELETE_BAD_GIFT = 31;
    public static final int PREMIUM_ACCESS_ONLY = 32;
    public static final int NON_EXIST_PHOTO_ERROR = 33;
    public static final int INVALID_PURCHASE_TOKEN = 34;
    public static final int CANNOT_BECOME_LEADER = 35;
    public static final int CODE_VIRUS_LIKES_ALREADY_RECEIVED = 36;
    public static final int CODE_OLD_APPLICATION_VERSION = 37;
    public static final int BLACKLIST_MAX_LENGTH_REACHED = 38;
    public static final int USER_ALREADY_REGISTERED = 39;
    public static final int DATA_EXPIRED = 40;
    public static final int CANNOT_CHANGE_LOGIN = 41;
    public static final int INCORRECT_LOGIN = 42;
    public static final int INCORRECT_PASSWORD = 43;
    public static final int PHOTO_UPLOAD_ERROR = 46;
    public static final int INCORRECT_PHOTO_DATA = 47;
    public static final int INCORRECT_PHOTO_SIZES = 48;
    public static final int INCORRECT_PHOTO_FORMAT = 49;
    public static final int TOO_MANY_MESSAGES = 50;
    public static final int TOO_MANY_DAILY_MESSAGES = 51;
    public static final int ALREADY_AWARDED = 52;
    public static final int UNCONFIRMED_LOGIN = 53;
    public static final int USER_DELETED = 54;
    public static final int BLOCKED_SYMPATHIES = 57;
    public static final int BLOCKED_PEOPLE_NEARBY = 58;
}
