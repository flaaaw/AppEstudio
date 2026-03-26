package com.example.appestudio.data.local

import androidx.room.*
import com.example.appestudio.data.models.PostDto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val id: String,
    val author: String,
    val authorId: String,
    val title: String,
    val content: String,
    val mediaUrl: String?,
    val tags: List<String>,
    val likedBy: List<String>,
    val likes: Int,
    val comments: Int,
    val createdAt: String
)

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>): String = Gson().toJson(value)

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType)
    }
}

@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY createdAt DESC")
    suspend fun getAllPosts(): List<PostEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<PostEntity>)

    @Query("DELETE FROM posts")
    suspend fun clearAll()
}

@Database(entities = [PostEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

fun PostDto.toEntity() = PostEntity(
    id = _id,
    author = author,
    authorId = authorId,
    title = title,
    content = content,
    mediaUrl = mediaUrl,
    tags = tags,
    likedBy = likedBy,
    likes = likes,
    comments = comments,
    createdAt = createdAt
)

fun PostEntity.toDto() = PostDto(
    _id = id,
    author = author,
    authorId = authorId,
    title = title,
    content = content,
    mediaUrl = mediaUrl,
    tags = tags,
    likedBy = likedBy,
    likes = likes,
    comments = comments,
    createdAt = createdAt
)
