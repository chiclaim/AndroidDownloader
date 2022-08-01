package com.chiclaim.android.updater

import android.os.Parcel
import android.os.Parcelable

/**
 *
 * @author by chiclaim@google.com
 */
class UpgradeDialogInfo() : Parcelable {

    var url: String? = null
    var title: String? = null
    var description: String? = null
    var negativeText: String? = null
    var positiveText: String? = null
    var ignoreLocal = false
    var notifierSmallIcon = -1
    var destinationPath: String? = null
    var forceUpdate = false
    var backgroundDownload = true

    constructor(parcel: Parcel) : this() {
        url = parcel.readString()
        title = parcel.readString()
        description = parcel.readString()
        negativeText = parcel.readString()
        positiveText = parcel.readString()
        ignoreLocal = parcel.readInt() != 0
        notifierSmallIcon = parcel.readInt()
        destinationPath = parcel.readString()
        forceUpdate = parcel.readInt() != 0
        backgroundDownload = parcel.readInt() != 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(url)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeString(negativeText)
        parcel.writeString(positiveText)
        parcel.writeInt(if (ignoreLocal) 1 else 0)
        parcel.writeInt(notifierSmallIcon)
        parcel.writeString(destinationPath)
        parcel.writeInt(if (forceUpdate) 1 else 0)
        parcel.writeInt(if (backgroundDownload) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UpgradeDialogInfo> {
        override fun createFromParcel(parcel: Parcel): UpgradeDialogInfo {
            return UpgradeDialogInfo(parcel)
        }

        override fun newArray(size: Int): Array<UpgradeDialogInfo?> {
            return arrayOfNulls(size)
        }
    }
}