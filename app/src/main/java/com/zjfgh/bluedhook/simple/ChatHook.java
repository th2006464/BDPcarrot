package com.zjfgh.bluedhook.simple;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.XModuleResources;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public class ChatHook {
    private final ClassLoader classLoader;
    private static ChatHook instance;
    private final WeakReference<Context> contextRef;
    private final XModuleResources modRes;

    private ChatHook(Context context, XModuleResources modRes) {
        this.contextRef = new WeakReference<>(context);
        this.classLoader = context.getClassLoader();
        this.modRes = modRes;
        messageRecallHook();
        snapChatHook();
        chatHelperV4MdHook();
        chatReadedHook();
        chatProtectScreenshotHook();
        hookMsgChattingTitle();
    }

    public static synchronized ChatHook getInstance(Context context, XModuleResources modRes) {
        if (instance == null) {
            instance = new ChatHook(context, modRes);
        }
        return instance;
    }

    public void messageRecallHook() {
        Class<?> PushMsgPackage = XposedHelpers.findClass("com.blued.android.chat.core.pack.PushMsgPackage", classLoader);
        XposedHelpers.findAndHookMethod("com.blued.android.chat.core.worker.chat.Chat", classLoader, "receiveOrderMessage",
                PushMsgPackage, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        Object pushMsgPackage = param.args[0];
                        short msgType = XposedHelpers.getShortField(pushMsgPackage, "msgType");
                        if (msgType == 55) {
                            long msgId = XposedHelpers.getLongField(pushMsgPackage, "msgId");
                            short sessionType = XposedHelpers.getShortField(pushMsgPackage, "sessionType");
                            long sessionId = XposedHelpers.getLongField(pushMsgPackage, "sessionId");
                            Class<?> ChatManager = XposedHelpers.findClass("com.blued.android.chat.ChatManager", classLoader);
                            Object dbOperImpl = XposedHelpers.getStaticObjectField(ChatManager, "dbOperImpl");
                            Object originalMsg = XposedHelpers.callMethod(
                                    dbOperImpl,
                                    "findMsgData",
                                    sessionType,
                                    sessionId,
                                    msgId,
                                    0L
                            );
                            if (originalMsg != null) {
                                short originalType = XposedHelpers.getShortField(originalMsg, "msgType");
                                if (originalType == 55) {
                                    return;
                                }
                                XposedHelpers.setShortField(pushMsgPackage, "msgType", originalType);
                                XposedHelpers.setShortField(originalMsg, "msgType", originalType);
                                XposedHelpers.callMethod(dbOperImpl, "updateChattingModel", originalMsg);
                            }
                        }
                        param.setResult(null);
                    }
                });
    }

    public void chatHelperV4MdHook() {
        Class<?> ChattingModel = XposedHelpers.findClass("com.blued.android.chat.model.ChattingModel", classLoader);
        XposedHelpers.findAndHookMethod("com.soft.blued.ui.msg.controller.tools.ChatHelperV4", classLoader, "b", android.content.Context.class, ChattingModel, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                handleChatMessage(param, "b");
            }

            @Override
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });
        XposedHelpers.findAndHookMethod("com.soft.blued.ui.msg.controller.tools.ChatHelperV4", classLoader, "c", android.content.Context.class, ChattingModel, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                handleChatMessage(param, "c");
            }

            @Override
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });
        XposedHelpers.findAndHookMethod("com.soft.blued.ui.msg.controller.tools.ChatHelperV4", classLoader, "d", android.content.Context.class, ChattingModel, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                handleChatMessage(param, "d");
            }

            @Override
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });
        XposedHelpers.findAndHookMethod("com.soft.blued.ui.msg.controller.tools.ChatHelperV4", classLoader, "e", android.content.Context.class, ChattingModel, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                handleChatMessage(param, "e");
            }

            @Override
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });
        XposedHelpers.findAndHookMethod("com.soft.blued.ui.msg.controller.tools.ChatHelperV4", classLoader, "f", android.content.Context.class, ChattingModel, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                handleChatMessage(param, "f");
            }

            @Override
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });

        XposedHelpers.findAndHookMethod("com.soft.blued.ui.msg.presenter.MsgChattingPresent", classLoader, "c", ChattingModel, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });
    }

    public void snapChatHook() {
        try {
            XposedHelpers.findAndHookMethod(
                    "com.soft.blued.ui.msg.presenter.MsgChattingPresent",
                    classLoader,
                    "F",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            getTvRecallMsg().setVisibility(View.VISIBLE);
                            handleFlashMessages(param);
                        }
                    });
        } catch (Throwable e) {
        }
    }

    private void handleFlashMessages(XC_MethodHook.MethodHookParam param) {
        try {
            Object thisObject = param.thisObject;
            Object t = XposedHelpers.getObjectField(thisObject, "t");
            Object E = XposedHelpers.callMethod(t, "E");
            Object a = XposedHelpers.callMethod(E, "a");
            if (!(a instanceof List)) {
                return;
            }

            for (Object msg : (List<?>) a) {
                processSingleMessage(msg);
            }
        } catch (Throwable e) {
        }
    }

    private void processSingleMessage(Object msgObj) {
        try {
            short msgType = XposedHelpers.getShortField(msgObj, "msgType");
            String msgContent = (String) XposedHelpers.getObjectField(msgObj, "msgContent");
            Class<?> fieldType = XposedHelpers.findField(msgObj.getClass(), "msgType").getType();
            if (!(fieldType == short.class || fieldType == Short.class)) {
                return;
            }
            boolean isFromSelf = (boolean) XposedHelpers.callMethod(msgObj, "isFromSelf");
            if (isFromSelf) {
                return;
            }
            switch (msgType) {
                case 24:
                    convertFlashMessage(msgObj, (short) 2, msgContent, "照片");
                    break;
                case 25:
                    convertFlashMessage(msgObj, (short) 5, msgContent, "视频");
                    break;
            }
        } catch (Throwable e) {
        }
    }

    private void convertFlashMessage(Object msgObj, short newType, String encryptedContent, String typeName) {
        try {
            short msgType = XposedHelpers.getShortField(msgObj, "msgType");
            if (msgType == 55) {
                XposedHelpers.setShortField(msgObj, "msgType", newType);
                XposedHelpers.setAdditionalInstanceField(msgObj, "oldMsgType", msgType);
            } else {
                XposedHelpers.setShortField(msgObj, "msgType", newType);
                XposedHelpers.setAdditionalInstanceField(msgObj, "oldMsgType", msgType);
                String decryptedContent = ModuleTools.AesDecrypt(encryptedContent);
                XposedHelpers.setObjectField(msgObj, "msgContent", decryptedContent);
            }
        } catch (Throwable e) {
        }
    }

    public static class ChatContent {
        public int msgType;
        public String fromNickName;
        public String extraMsg;
        public String msgContent;
        public long sessionId;
        public long fromId;
        public String fromAvatar;
    }

    private void handleChatMessage(XC_MethodHook.MethodHookParam param, String methodTag) {
        if (param.args == null || param.args.length < 2) {
            return;
        }
        try {
            Object chattingModel = param.args[1];
            ChatContent chatContent = new ChatContent();
            chatContent.msgType = XposedHelpers.getIntField(chattingModel, "msgType");
            chatContent.fromNickName = (String) XposedHelpers.getObjectField(chattingModel, "fromNickName");
            chatContent.extraMsg = (String) XposedHelpers.callMethod(chattingModel, "getMsgExtra");
            chatContent.msgContent = (String) XposedHelpers.getObjectField(chattingModel, "msgContent");
            chatContent.sessionId = XposedHelpers.getLongField(chattingModel, "sessionId");
            chatContent.fromId = XposedHelpers.getLongField(chattingModel, "fromId");
            switch (chatContent.msgType) {
                case 24:
                    XposedHelpers.setIntField(chattingModel, "msgType", 2);
                    break;
                case 25:
                    XposedHelpers.setIntField(chattingModel, "msgType", 5);
                    XposedHelpers.setObjectField(chattingModel, "msgContent", ModuleTools.AesDecrypt(chatContent.msgContent));
                    break;
            }
        } catch (Exception e) {
        }
    }

    public void chatReadedHook() {
        XposedHelpers.findAndHookMethod("io.grpc.MethodDescriptor", classLoader, "generateFullMethodName", String.class, String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                String serviceName = (String) param.args[0];
                String methodName = (String) param.args[1];
                if (serviceName.equals("com.blued.im.private_chat.Receipt") && methodName.equals("Read")) {
                    param.args[0] = "";
                    param.args[1] = "";
                }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });
    }

    public void chatProtectScreenshotHook() {
        XposedHelpers.findAndHookMethod("com.soft.blued.ui.msg.MsgChattingFragment", classLoader, "c", boolean.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                if ((boolean) param.args[0]) {
                    param.args[0] = false;
                    getTvScreenshotProtection().setVisibility(View.VISIBLE);
                }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });
    }

    private TextView tv_chat_read_msg;
    private TextView tv_recall_msg;
    private TextView tv_screenshot_protection;

    public TextView getTvScreenshotProtection() {
        return tv_screenshot_protection;
    }

    public TextView getTvRecallMsg() {
        return tv_recall_msg;
    }

    private void hookMsgChattingTitle() {
        XposedHelpers.findAndHookMethod("com.soft.blued.ui.msg.MsgChattingFragment", classLoader, "af", new XC_MethodHook() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                View n = (View) XposedHelpers.getObjectField(param.thisObject, "n");
                @SuppressLint("DiscouragedApi") int msg_chatting_titleId = getSafeContext().getResources().getIdentifier("msg_chatting_title", "id", getSafeContext().getPackageName());
                View findViewById = n.findViewById(msg_chatting_titleId);
                @SuppressLint("DiscouragedApi") int ll_center_distanceId = getSafeContext().getResources().getIdentifier("ll_center_distance", "id", getSafeContext().getPackageName());
                LinearLayout ll_center_distance = findViewById.findViewById(ll_center_distanceId);
                TagLayout tlTitle = new TagLayout(n.getContext());
                tv_chat_read_msg = tlTitle.addTextView("悄悄查看", 9, modRes.getDrawable(R.drawable.bg_orange, null));
                tv_recall_msg = tlTitle.addTextView("防撤回", 9, modRes.getDrawable(R.drawable.bg_gradient_orange, null));
                tv_recall_msg.setVisibility(View.GONE);
                tv_screenshot_protection = tlTitle.addTextView("私信截图", 9, modRes.getDrawable(R.drawable.bg_rounded, null));
                tv_screenshot_protection.setVisibility(View.GONE);
                ll_center_distance.addView(tlTitle);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });
    }

    private Context getSafeContext() {
        return contextRef != null ? contextRef.get() : null;
    }
}
