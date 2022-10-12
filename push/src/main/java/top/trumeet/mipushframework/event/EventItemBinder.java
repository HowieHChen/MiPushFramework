package top.trumeet.mipushframework.event;

import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.xiaomi.push.service.MIPushEventProcessor;
import com.xiaomi.push.service.MyMIPushNotificationHelper;
import com.xiaomi.xmpush.thrift.XmPushActionContainer;
import com.xiaomi.xmsf.R;

import java.util.Date;

import top.trumeet.common.event.Event;
import top.trumeet.common.event.type.EventType;
import top.trumeet.common.event.type.TypeFactory;
import top.trumeet.common.utils.Utils;
import top.trumeet.mipushframework.permissions.ManagePermissionsActivity;
import top.trumeet.mipushframework.utils.BaseAppsBinder;
import top.trumeet.mipushframework.utils.ParseUtils;

/**
 * Created by Trumeet on 2017/8/26.
 * @see Event
 * @see EventFragment
 * @author Trumeet
 */

public class EventItemBinder extends BaseAppsBinder<Event> {

    private boolean isSpecificApp = true;
    EventItemBinder(boolean isSpecificApp) {
        super();
        this.isSpecificApp = isSpecificApp;
    }

    @Override
    protected void onBindViewHolder(final @NonNull ViewHolder holder, final @NonNull Event item) {
        fillData(item.getPkg(), false, holder);
        final EventType type = TypeFactory.create(item, item.getPkg());
        holder.title.setText(type.getTitle(holder.itemView.getContext()));
        holder.summary.setText(type.getSummary(holder.itemView.getContext()));

        String status = "";
        switch (item.getResult()) {
            case Event.ResultType.OK :
                if (item.getPayload() != null) {
                    XmPushActionContainer container = MIPushEventProcessor.buildContainer(item.getPayload());
                    if (container.metaInfo.isSetPassThrough()) {
                        if (container.metaInfo.passThrough == 0) {
                            status = holder.itemView.getContext()
                                    .getString(R.string.message_type_notification);
                        } else if (container.metaInfo.passThrough == 1) {
                            status = holder.itemView.getContext()
                                    .getString(R.string.message_type_pass_through);
                        }
                    }
                }
                break;
            case Event.ResultType.DENY_DISABLED:
                status = holder.itemView.getContext()
                        .getString(R.string.status_deny_disable);
                break;
            case Event.ResultType.DENY_USER:
                status = holder.itemView.getContext()
                        .getString(R.string.status_deny_user);
                break;
            default:
                break;
        }
        holder.text2.setText(
                ParseUtils.getFriendlyDateString(new Date(item.getDate()),
                        Utils.getUTC(), holder.itemView.getContext()));
        holder.status.setText(status);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = createInfoDialog(type,
                        holder.itemView.getContext()); // "Developer info" dialog for event messages
                if (dialog != null && isSpecificApp) {
                    dialog.show();
                } else {
                    startManagePermissions(type, holder.itemView.getContext());
                }
            }
        });
    }

    @Nullable
    private Dialog createInfoDialog (final EventType type, final Context context) {
        final CharSequence info = type.getInfo(context);
        if (info == null)
            return null;
        AlertDialog.Builder build = new AlertDialog.Builder(context)
                .setMessage(info)
                .setNeutralButton(android.R.string.copy, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ClipboardManager clipboardManager = (ClipboardManager)
                                context.getSystemService(Context.CLIPBOARD_SERVICE);
                        clipboardManager.setText(info);
                    }
                })
                .setNegativeButton(R.string.action_edit_permission, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startManagePermissions(type, context);
                    }
                });
        if (type.getPayload() != null) {
            build.setPositiveButton(R.string.action_notify, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    MyMIPushNotificationHelper.notifyPushMessage(context,
                            MIPushEventProcessor.buildContainer(type.getPayload()),
                            type.getPayload()
                    );
                }
            });
        }

        return build.create();
    }

    private static void startManagePermissions (EventType type, Context context) {
        // Issue: This currently allows overlapping opens.
        context.startActivity(new Intent(context,
                ManagePermissionsActivity.class)
                .putExtra(ManagePermissionsActivity.EXTRA_PACKAGE_NAME,
                        type.getPkg()));
    }
}
