package wee.vietinbank.kiosk.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Entity(tableName = "enrolls")
class EnrollDBO {

    @ColumnInfo(name = "enroll_id")
    @PrimaryKey
    var id: Long = System.currentTimeMillis()

    var label: String? = null

    @ColumnInfo(name = "enroll_crop_color", typeAffinity = ColumnInfo.BLOB)
    var cropColorImage: ByteArray? = null

    @ColumnInfo(name = "enroll_crop_depth", typeAffinity = ColumnInfo.BLOB)
    var cropDepthImage: ByteArray? = null

    constructor()

    @Dao
    interface DAO {

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun insert(t: EnrollDBO)

        @Query("SELECT * FROM enrolls")
        fun data(): List<EnrollDBO>

        @Query("SELECT * FROM enrolls")
        fun liveData(): LiveData<List<EnrollDBO>>

        @Delete
        fun delete(t: EnrollDBO)
    }

}