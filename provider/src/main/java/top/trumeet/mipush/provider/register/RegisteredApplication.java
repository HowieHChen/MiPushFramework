package top.trumeet.mipush.provider.register;

import static java.lang.annotation.RetentionPolicy.SOURCE;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Unique;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Created by Trumeet on 2017/8/26.
 * A registered application. Can 3 types: {@link Type#ALLOW}, {@link Type#DENY}
 * or {@link Type#ASK}.
 * Default is Ask: show a dialog to request push permission.
 * It will auto create using ask type when application register push.
 *
 * This entity will also save application's push permissions.
 *
 * @author Trumeet
 */

@Entity
public class RegisteredApplication implements Parcelable {
    protected RegisteredApplication(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        packageName = in.readString();
        type = in.readInt();
        allowReceivePush = in.readByte() != 0;
        allowReceiveRegisterResult = in.readByte() != 0;
        allowReceiveCommand = in.readByte() != 0;
    }

    public static final Creator<RegisteredApplication> CREATOR = new Creator<RegisteredApplication>() {
        @Override
        public RegisteredApplication createFromParcel(Parcel in) {
            return new RegisteredApplication(in);
        }

        @Override
        public RegisteredApplication[] newArray(int size) {
            return new RegisteredApplication[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        if (id == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeLong(id);
        }
        parcel.writeString(packageName);
        parcel.writeInt(type);
        parcel.writeByte((byte) (allowReceivePush ? 1 : 0));
        parcel.writeByte((byte) (allowReceiveRegisterResult ? 1 : 0));
        parcel.writeByte((byte) (allowReceiveCommand ? 1 : 0));
    }

    @IntDef({Type.ASK, Type.ALLOW, Type.DENY, Type.ALLOW_ONCE})
    @Retention(SOURCE)
    @Target({ElementType.PARAMETER, ElementType.TYPE,
            ElementType.FIELD, ElementType.METHOD})
    public @interface Type {
        int ASK = 0;
        int ALLOW = 2;
        int DENY = 3;
        int ALLOW_ONCE = -1;
    }

    @Id
    private Long id;

    @Unique
    @Property(nameInDb = "pkg")
    private String packageName;

    @Type
    @Property(nameInDb = "type")
    private int type = Type.ASK;

    @Property(nameInDb = "allow_receive_push")
    private boolean allowReceivePush;

    @Property(nameInDb = "allow_receive_register_result")
    private boolean allowReceiveRegisterResult;

    @Property(nameInDb = "allow_receive_command_without_register_result")
    private boolean allowReceiveCommand;

    @Property(nameInDb = "notification_on_register")
    private boolean notificationOnRegister;

    @Property(nameInDb = "group_notifications_for_same_session")
    private boolean groupNotificationsForSameSession;

    @Property(nameInDb = "clear_all_notifications_of_session")
    private boolean clearAllNotificationsOfSession;

    @Property(nameInDb = "show_pass_through")
    private boolean showPassThrough;

    @Property(nameInDb = "group_notifications_by_title")
    private boolean groupNotificationsByTitle;

    @Generated(hash = 409356025)
    public RegisteredApplication(Long id, String packageName, int type, boolean allowReceivePush,
            boolean allowReceiveRegisterResult, boolean allowReceiveCommand, boolean notificationOnRegister,
            boolean groupNotificationsForSameSession, boolean clearAllNotificationsOfSession, boolean showPassThrough,
            boolean groupNotificationsByTitle) {
        this.id = id;
        this.packageName = packageName;
        this.type = type;
        this.allowReceivePush = allowReceivePush;
        this.allowReceiveRegisterResult = allowReceiveRegisterResult;
        this.allowReceiveCommand = allowReceiveCommand;
        this.notificationOnRegister = notificationOnRegister;
        this.groupNotificationsForSameSession = groupNotificationsForSameSession;
        this.clearAllNotificationsOfSession = clearAllNotificationsOfSession;
        this.showPassThrough = showPassThrough;
        this.groupNotificationsByTitle = groupNotificationsByTitle;
    }

    public RegisteredApplication() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setAllowReceivePush(boolean allowReceivePush) {
        this.allowReceivePush = allowReceivePush;
    }

    public boolean getAllowReceivePush() {
        return this.allowReceivePush;
    }

    public boolean getAllowReceiveRegisterResult() {
        return this.allowReceiveRegisterResult;
    }

    public void setAllowReceiveRegisterResult(boolean allowReceiveRegisterResult) {
        this.allowReceiveRegisterResult = allowReceiveRegisterResult;
    }

    @NonNull
    public CharSequence getLabel (Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            return pm.getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES).loadLabel(pm);
        } catch (PackageManager.NameNotFoundException e) {
            return packageName;
        } catch (Resources.NotFoundException e) {   // Not using multi-catch due to build error from greendao
            return packageName;
        }
    }

    @NonNull
    public Drawable getIcon (Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            return pm.getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES).loadIcon(pm);
        } catch (PackageManager.NameNotFoundException e) {
            return ContextCompat.getDrawable(context, android.R.mipmap.sym_def_app_icon);
        } catch (Resources.NotFoundException e) {   // Not using multi-catch due to build error from greendao
            return ContextCompat.getDrawable(context, android.R.mipmap.sym_def_app_icon);
        }
    }

    public int getUid (Context context) {
        try {
            return context.getPackageManager().getApplicationInfo(packageName,
                    PackageManager.GET_UNINSTALLED_PACKAGES)
                    .uid;
        } catch (PackageManager.NameNotFoundException e) {
            return -1;
        }
    }

    public boolean isNotificationOnRegister() {
        return notificationOnRegister;
    }

    public void setNotificationOnRegister(boolean notificationOnRegister) {
        this.notificationOnRegister = notificationOnRegister;
    }

    public top.trumeet.common.register.RegisteredApplication convertTo () {
        return convertTo(this);
    }

    /**
     * 将 Provider 中的 Model 转换成 Xmsf 中用的 {@link top.trumeet.common.register.RegisteredApplication}
     * （非常辣鸡
     * @param original Original model
     * @return Target model
     */
    @NonNull
    public static top.trumeet.common.register.RegisteredApplication convertTo
    (@NonNull RegisteredApplication original) {
        return new top.trumeet.common.register.RegisteredApplication(original.id
                , original.packageName
                , original.type, original.allowReceivePush
                , original.allowReceiveRegisterResult
                , original.allowReceiveCommand
                , 0
                , original.notificationOnRegister
                , original.groupNotificationsForSameSession
                , original.clearAllNotificationsOfSession
                , original.showPassThrough
                , original.groupNotificationsByTitle
                );
    }

    @NonNull
    public static RegisteredApplication from (@NonNull top.trumeet.common.register.RegisteredApplication original) {
        return new RegisteredApplication(original.getId()
                , original.getPackageName()
                , original.getType()
                , original.getAllowReceivePush()
                , original.getAllowReceiveRegisterResult()
                , original.isAllowReceiveCommand()
                , original.isNotificationOnRegister()
                , original.isGroupNotificationsForSameSession()
                , original.isClearAllNotificationsOfSession()
                , original.isShowPassThrough()
                , original.isGroupNotificationsByTitle()
                );
    }

    public boolean isAllowReceiveCommand() {
        return allowReceiveCommand;
    }

    public void setAllowReceiveCommand(boolean allowReceiveCommand) {
        this.allowReceiveCommand = allowReceiveCommand;
    }

    public boolean getAllowReceiveCommand() {
        return this.allowReceiveCommand;
    }

    public boolean getNotificationOnRegister() {
        return this.notificationOnRegister;
    }

    public boolean getGroupNotificationsForSameSession() {
        return this.groupNotificationsForSameSession;
    }

    public void setGroupNotificationsForSameSession(boolean groupNotificationsForSameSession) {
        this.groupNotificationsForSameSession = groupNotificationsForSameSession;
    }

    public boolean getClearAllNotificationsOfSession() {
        return this.clearAllNotificationsOfSession;
    }

    public void setClearAllNotificationsOfSession(boolean clearAllNotificationsOfSession) {
        this.clearAllNotificationsOfSession = clearAllNotificationsOfSession;
    }

    public boolean getShowPassThrough() {
        return this.showPassThrough;
    }

    public void setShowPassThrough(boolean showPassThrough) {
        this.showPassThrough = showPassThrough;
    }

    public boolean getGroupNotificationsByTitle() {
        return this.groupNotificationsByTitle;
    }

    public void setGroupNotificationsByTitle(boolean groupNotificationsByTitle) {
        this.groupNotificationsByTitle = groupNotificationsByTitle;
    }
}
