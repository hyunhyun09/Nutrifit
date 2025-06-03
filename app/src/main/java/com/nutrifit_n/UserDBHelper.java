package com.nutrifit_n;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UserDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "user_info.db";
    private static final int DATABASE_VERSION = 1;

    public UserDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static final String TABLE_NAME = "user_info";
    private static final String COLUMN_HEIGHT = "height";
    private static final String COLUMN_WEIGHT = "weight";
    private static final String COLUMN_AGE = "age";
    private static final String COLUMN_GENDER = "gender";
    private static final String COLUMN_ACTIVITY = "activity";
    private static final String COLUMN_TASTE1 = "taste1";
    private static final String COLUMN_TASTE2 = "taste2";

    // 테이블 생성 쿼리 수정
    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_HEIGHT + " REAL NOT NULL, " +
                    COLUMN_WEIGHT + " REAL NOT NULL, " +
                    COLUMN_AGE + " INTEGER NOT NULL, " +
                    COLUMN_GENDER + " INTEGER NOT NULL, " +
                    COLUMN_ACTIVITY + " INTEGER NOT NULL, " +
                    COLUMN_TASTE1 + " INTEGER DEFAULT 0, " +
                    COLUMN_TASTE2 + " INTEGER DEFAULT 0)";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 기존 테이블 삭제
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // 사용자 정보와 taste 데이터를 저장
    public void insertUser(double height, double weight, int age, int gender, int activity,
                           int taste1, int taste2) {
        SQLiteDatabase db = this.getWritableDatabase();

        // 기존 테이블 초기화
        db.execSQL("DELETE FROM " + TABLE_NAME);

        ContentValues values = new ContentValues();
        values.put(COLUMN_HEIGHT, height);
        values.put(COLUMN_WEIGHT, weight);
        values.put(COLUMN_AGE, age);
        values.put(COLUMN_GENDER, gender);
        values.put(COLUMN_ACTIVITY, activity);
        values.put(COLUMN_TASTE1, taste1);
        values.put(COLUMN_TASTE2, taste2);

        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    // 입력된 사용자 정보 확인
    public Cursor getUserInfo() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }
}
