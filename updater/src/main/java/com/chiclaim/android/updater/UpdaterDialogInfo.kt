package com.chiclaim.android.updater

import android.os.Parcel
import android.os.Parcelable

/**
 *
 * @author by chiclaim@google.com
 */
class UpdaterDialogInfo() : Parcelable {

    var url: String? = null
    var title: String? = null
    var description: String? = null
    var leftButton: String? = null
    var rightButton: String? = null

    constructor(parcel: Parcel) : this() {
        url = parcel.readString()
        title = parcel.readString()
        description = parcel.readString()
        leftButton = parcel.readString()
        rightButton = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(url)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeString(leftButton)
        parcel.writeString(rightButton)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UpdaterDialogInfo> {
        override fun createFromParcel(parcel: Parcel): UpdaterDialogInfo {
            return UpdaterDialogInfo(parcel)
        }

        override fun newArray(size: Int): Array<UpdaterDialogInfo?> {
            return arrayOfNulls(size)
        }
    }
}