package wee.vietinbank.kiosk.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import wee.vietinbank.kiosk.App
import wee.vietinbank.kiosk.BuildConfig

@Database(
        entities = [EnrollDBO::class],
        version = 1,
        exportSchema = false
)
abstract class MyDB : RoomDatabase() {

    abstract val enrollDao: EnrollDBO.DAO

    companion object {

        val instance: MyDB by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            initDatabase(App.instance.applicationContext)
        }

        private fun initDatabase(context: Context): MyDB {
            return Room.databaseBuilder(context, MyDB::class.java, BuildConfig.APPLICATION_ID + ".test")
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build()
        }

    }

}