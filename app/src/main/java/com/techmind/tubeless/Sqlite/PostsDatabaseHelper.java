package com.techmind.tubeless.Sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import com.techmind.tubeless.models.YoutubeDataModel;
import com.techmind.tubeless.pojo.User;

import java.util.ArrayList;
import java.util.List;

import static com.techmind.tubeless.config.AppController.TAG;
import static com.techmind.tubeless.config.ConstURL.CHANNEL_TYPE;
import static com.techmind.tubeless.config.ConstURL.PLAYLIST_TYPE;
import static com.techmind.tubeless.config.ConstURL.VIDEOS_TYPE;

public class PostsDatabaseHelper extends SQLiteOpenHelper {
    // Database Info
    private static final String DATABASE_NAME = "bookmarkDatabase";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_BOOKMARK = "bookmarkTable";

    // Post Table Columns
    private static final String KEY_POST_ID = "id";
    private static final String KEY_BM_TYPE = "bookMarkType";
    private static final String KEY_BM_TYPE_ID = "bookMarkTypeID";
    private static final String KEY_BM_THUMBNAIL_HIGH_URL = "bookMarkImgThumbnailHighUrl";
    private static final String KEY_BM_THUMBNAIL_MEDIUM_URL = "bookMarkImgThumbnailMediumUrl";
    private static final String KEY_BM_THUMBNAIL_DEFAULT_URL = "bookMarkImgThumbnailDefaultUrl";
    private static final String KEY_BM_DESCRIPTION = "bookMarkDescription";
    private static final String KEY_BM_PUBLISHED_AT = "bookMarkPublishedAt";
    private static final String KEY_BM_TITLE = "bookMarkTitle";
    private static final String KEY_BM_CHANNEL_TITLE_AT = "bookMarkChannelTitle";
    private static final String KEY_BM_VIEW_COUNT = "bookMarkViewCount";
    private static final String KEY_BM_LIKE_COUNT = "bookMarkLikeCount";
    private static final String KEY_BM_DISLIKE_COUNT = "bookMarkDislikeCount";
    private static final String KEY_BM_FAVORITE_COUNT = "bookMarkFavoriteCount";
    private static final String KEY_BM_COMMENT_COUNT = "bookMarkCommentCount";
    private static final String KEY_BM_SUBSCRIBER_COUNT = "bookMarkSubscriberCount";
    private static final String KEY_BM_VIDEO_COUNT = "bookMarkVideoCount";
    private static final String KEY_BM_PLAYLIST_COUNT = "bookMarkPlayListCount";
    private static final String KEY_BM_DURATION_COUNT = "bookMarkDurationCount";
    // User Table Columns
//    private static final String KEY_USER_ID = "id";
//    private static final String KEY_USER_NAME = "userName";
//    private static final String KEY_USER_PROFILE_PICTURE_URL = "profilePictureUrl";

    private static PostsDatabaseHelper sInstance;
    private long userId;

    public static synchronized PostsDatabaseHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new PostsDatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Constructor should be private to prevent direct instantiation.
     * Make a call to the static method "getInstance()" instead.
     */
    private PostsDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Called when the database connection is being configured.
    // Configure database settings for things like foreign key support, write-ahead logging, etc.
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            db.setForeignKeyConstraintsEnabled(true);
        }
    }

    // Called when the database is created for the FIRST time.
    // If a database already exists on disk with the same DATABASE_NAME, this method will NOT be called.
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_BOOKMARK_TABLE = "CREATE TABLE " + TABLE_BOOKMARK + "(" +
                KEY_POST_ID + " INTEGER PRIMARY KEY," + KEY_BM_TYPE + " TEXT," + KEY_BM_TITLE + " TEXT," + KEY_BM_TYPE_ID +
                " TEXT," + KEY_BM_THUMBNAIL_HIGH_URL + " TEXT ," + KEY_BM_THUMBNAIL_MEDIUM_URL + " TEXT ," +
                KEY_BM_THUMBNAIL_DEFAULT_URL + " TEXT ," + KEY_BM_PUBLISHED_AT + " TEXT ,"
                + KEY_BM_VIEW_COUNT + " TEXT ," + KEY_BM_LIKE_COUNT + " TEXT ," + KEY_BM_DISLIKE_COUNT + " TEXT " +
                "," + KEY_BM_FAVORITE_COUNT + " TEXT ," + KEY_BM_CHANNEL_TITLE_AT + " TEXT ,"
                + KEY_BM_COMMENT_COUNT + " TEXT ," + KEY_BM_SUBSCRIBER_COUNT + " TEXT ,"
                + KEY_BM_VIDEO_COUNT + " TEXT ," +KEY_BM_PLAYLIST_COUNT  + " TEXT ," +KEY_BM_DURATION_COUNT  + " TEXT ," + KEY_BM_DESCRIPTION + " TEXT" + ")";

        db.execSQL(CREATE_BOOKMARK_TABLE);
    }

    // Called when the database needs to be upgraded.
    // This method will only be called if a database already exists on disk with the same DATABASE_NAME,
    // but the DATABASE_VERSION is different than the version of the database that exists on disk.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKMARK);
            onCreate(db);
        }
    }

    // Insert a post into the database
    public boolean addPost(YoutubeDataModel youtubeDataModel, String type) {
        // Create and/or open the database for writing
        SQLiteDatabase db = this.getWritableDatabase();
        boolean insertOrNot = false;
        // It's a good idea to wrap our insert in a transaction. This helps with performance and ensures
        // consistency of the database.
        try {
            ContentValues values = new ContentValues();

            if (type.equalsIgnoreCase(CHANNEL_TYPE)) {
                values.put(KEY_BM_TYPE_ID, youtubeDataModel.getChannel_id());
            } else if (type.equalsIgnoreCase(VIDEOS_TYPE)) {
                values.put(KEY_BM_TYPE_ID, youtubeDataModel.getVideo_id());
            } else if (type.equalsIgnoreCase(PLAYLIST_TYPE)) {
                values.put(KEY_BM_TYPE_ID, youtubeDataModel.getPlayList_id());
            }
            System.out.println("values = " + values);
            values.put(KEY_BM_VIEW_COUNT, youtubeDataModel.getViewCount());
            values.put(KEY_BM_LIKE_COUNT, youtubeDataModel.getLikeCount());
            values.put(KEY_BM_DISLIKE_COUNT, youtubeDataModel.getDislikeCount());
            values.put(KEY_BM_FAVORITE_COUNT, youtubeDataModel.getFavoriteCount());
            values.put(KEY_BM_COMMENT_COUNT, youtubeDataModel.getCommentCount());
            values.put(KEY_BM_SUBSCRIBER_COUNT, youtubeDataModel.getSubscriberCount());
            values.put(KEY_BM_VIDEO_COUNT, youtubeDataModel.getVideoCount());
            values.put(KEY_BM_TYPE, type);
            values.put(KEY_BM_THUMBNAIL_HIGH_URL, youtubeDataModel.getThumbnailHigh());
            values.put(KEY_BM_THUMBNAIL_MEDIUM_URL, youtubeDataModel.getThumbnailMedium());
            values.put(KEY_BM_THUMBNAIL_DEFAULT_URL, youtubeDataModel.getThumbnailDefault());
            values.put(KEY_BM_PUBLISHED_AT, youtubeDataModel.getPublishedAt());
            values.put(KEY_BM_TITLE, youtubeDataModel.getTitle());
            values.put(KEY_BM_CHANNEL_TITLE_AT, youtubeDataModel.getChannelTitle());
            values.put(KEY_BM_DESCRIPTION, youtubeDataModel.getDescription());
            values.put(KEY_BM_PLAYLIST_COUNT , youtubeDataModel.getPlayListCount());
            values.put(KEY_BM_DURATION_COUNT , youtubeDataModel.getDuration());
            db.insert(TABLE_BOOKMARK, null, values);
            System.out.println("*****************Data insert successfully***************");
            insertOrNot = true;
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to add post to database******************" + e);
        } finally {
            db.close();
        }
        return insertOrNot;
    }

    // Insert or update a user in the database
    // Since SQLite doesn't support "upsert" we need to fall back on an attempt to UPDATE (in case the
    // user already exists) optionally followed by an INSERT (in case the user does not already exist).
    // Unfortunately, there is a bug with the insertOnConflict method
    // (https://code.google.com/p/android/issues/detail?id=13045) so we need to fall back to the more
    // verbose option of querying for the user's primary key if we did an update.
    public long checkTypeIdExistsOrNot(String typeID) {
        // The database connection is cached so it's not expensive to call getWriteableDatabase() multiple times.
        SQLiteDatabase db = this.getReadableDatabase();
        long userId = -1;
        try {

            // Get the primary key of the user we just updated
            String usersSelectQuery = String.format("SELECT * FROM %s WHERE %s = ?",
                    TABLE_BOOKMARK, KEY_BM_TYPE_ID);
            Cursor cursor = db.rawQuery(usersSelectQuery, new String[]{String.valueOf(typeID)});
            try {
                if (cursor.moveToFirst()) {
                    userId = cursor.getInt(0);
                    System.out.println("cursor In userId %%%%%%= " + userId);
                }
            } finally {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                    System.out.println("cursor null userId *****= " + userId);
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to add or update user");
        } finally {
            db.close();
        }
        return userId;
    }

    public Boolean deleteId(String typeID) {
        SQLiteDatabase db = this.getWritableDatabase();
        Boolean deletedOrNot = db.delete(TABLE_BOOKMARK, KEY_BM_TYPE_ID + " = ?", new String[]{String.valueOf(typeID)}) > 0;
        db.close();
        return deletedOrNot;
    }

    // Get all posts in the database
    public List<YoutubeDataModel> getAllPosts() {
        List<YoutubeDataModel> bookMarkArrayList = new ArrayList<>();

        // SELECT * FROM POSTS
        // LEFT OUTER JOIN USERS
        // ON POSTS.KEY_BM_TYPE = USERS.KEY_USER_ID
        String POSTS_SELECT_QUERY =
                String.format("SELECT * FROM %s",
                        TABLE_BOOKMARK);

        // "getReadableDatabase()" and "getWriteableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(POSTS_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    YoutubeDataModel youtubeDataModel = new YoutubeDataModel();
                    String type = cursor.getString(cursor.getColumnIndex(KEY_BM_TYPE));
                    if (type.equalsIgnoreCase(CHANNEL_TYPE)) {
                        youtubeDataModel.setChannel_id(cursor.getString(cursor.getColumnIndex(KEY_BM_TYPE_ID)));
                    } else if (type.equalsIgnoreCase(VIDEOS_TYPE)) {
                        youtubeDataModel.setVideo_id(cursor.getString(cursor.getColumnIndex(KEY_BM_TYPE_ID)));
                    } else if (type.equalsIgnoreCase(PLAYLIST_TYPE)) {
                        youtubeDataModel.setPlayList_id(cursor.getString(cursor.getColumnIndex(KEY_BM_TYPE_ID)));
                    }
                    youtubeDataModel.setDescription(cursor.getString(cursor.getColumnIndex(KEY_BM_DESCRIPTION)));
                    youtubeDataModel.setKind(cursor.getString(cursor.getColumnIndex(KEY_BM_TYPE)));
                    youtubeDataModel.setTitle(cursor.getString(cursor.getColumnIndex(KEY_BM_TITLE)));
                    youtubeDataModel.setThumbnailHigh(cursor.getString(cursor.getColumnIndex(KEY_BM_THUMBNAIL_HIGH_URL)));
                    youtubeDataModel.setThumbnailMedium(cursor.getString(cursor.getColumnIndex(KEY_BM_THUMBNAIL_MEDIUM_URL)));
                    youtubeDataModel.setThumbnailDefault(cursor.getString(cursor.getColumnIndex(KEY_BM_THUMBNAIL_DEFAULT_URL)));
                    youtubeDataModel.setPublishedAt(cursor.getString(cursor.getColumnIndex(KEY_BM_PUBLISHED_AT)));
                    youtubeDataModel.setChannelTitle(cursor.getString(cursor.getColumnIndex(KEY_BM_CHANNEL_TITLE_AT)));
                    youtubeDataModel.setChannelTitle(cursor.getString(cursor.getColumnIndex(KEY_BM_VIEW_COUNT)));
                    youtubeDataModel.setChannelTitle(cursor.getString(cursor.getColumnIndex(KEY_BM_LIKE_COUNT)));
                    youtubeDataModel.setChannelTitle(cursor.getString(cursor.getColumnIndex(KEY_BM_DISLIKE_COUNT)));
                    youtubeDataModel.setChannelTitle(cursor.getString(cursor.getColumnIndex(KEY_BM_FAVORITE_COUNT)));
                    youtubeDataModel.setChannelTitle(cursor.getString(cursor.getColumnIndex(KEY_BM_COMMENT_COUNT)));
                    youtubeDataModel.setChannelTitle(cursor.getString(cursor.getColumnIndex(KEY_BM_SUBSCRIBER_COUNT)));
                    youtubeDataModel.setChannelTitle(cursor.getString(cursor.getColumnIndex(KEY_BM_VIDEO_COUNT)));
                    youtubeDataModel.setChannelTitle(cursor.getString(cursor.getColumnIndex(KEY_BM_PLAYLIST_COUNT)));
                    youtubeDataModel.setChannelTitle(cursor.getString(cursor.getColumnIndex(KEY_BM_DURATION_COUNT)));
//                    Post newPost = new Post();
//                    newPost.text = cursor.getString(cursor.getColumnIndex(KEY_BM_TYPE_ID));
//                    newPost.user = newUser;
                    bookMarkArrayList.add(youtubeDataModel);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get posts from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return bookMarkArrayList;
    }

    // Update the user's profile picture url
    public int updateUserProfilePicture(User user) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_BM_TYPE_ID, user.profilePictureUrl);

        // Updating profile picture url for user with that userName
        return db.update(TABLE_BOOKMARK, values, KEY_BM_TYPE_ID + " = ?",
                new String[]{String.valueOf(user.userName)});
    }

    // Delete all posts and users in the database
    public void deleteAllPostsAndUsers() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            // Order of deletions is important when foreign key relationships exist.
            db.delete(TABLE_BOOKMARK, null, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to delete all posts and users");
        } finally {
            db.endTransaction();
        }
    }
}