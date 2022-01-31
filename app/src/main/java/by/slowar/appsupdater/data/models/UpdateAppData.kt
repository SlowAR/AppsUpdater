package by.slowar.appsupdater.data.models

import android.os.Parcel
import android.os.Parcelable

data class UpdateAppData(
    val appPackage: String,
    val description: String,
    val updateSize: String
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(appPackage)
        parcel.writeString(description)
        parcel.writeString(updateSize)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UpdateAppData> {
        override fun createFromParcel(parcel: Parcel): UpdateAppData {
            return UpdateAppData(parcel)
        }

        override fun newArray(size: Int): Array<UpdateAppData?> {
            return arrayOfNulls(size)
        }
    }
}