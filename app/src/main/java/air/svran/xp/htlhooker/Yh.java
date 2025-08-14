package air.svran.xp.htlhooker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * 功能介绍:  <br/>
 * 调用方式: / <br/>
 * <p/>
 * 作   者: Svran - 924633827@qq.com <br/>
 * 创建电脑: Svran-MY  <br/>
 * 创建时间: 2020/11/19 15:40 <br/>
 * 最后编辑: 2020/11/19 - Svran
 *
 * @author Svran
 */
public class Yh {

    public Yh(XC_LoadPackage.LoadPackageParam lpparam) {
        hook(lpparam);
    }

    public void hook(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedBridge.log("Svran: Hook 永辉");
        hookYh(lpparam.classLoader);
    }

    private Class<?> viewHolder;
    private Class<?> adapter;
    private final boolean debug = false;

    private void hookYh(ClassLoader classLoader) {
        boolean canHook = SvranHookUtils.findMethod("cn.yonghui.hyd.MyWrapperProxyApplication", "onCreate", classLoader, "hook 支持");
        if (!canHook) {
            XposedBridge.log("Svran: 不可hook 支持");
            return;
        }
        XposedHelpers.findAndHookMethod("cn.yonghui.hyd.MyWrapperProxyApplication", classLoader, "onCreate", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                XposedBridge.log("Svran: 进入永辉hook");
                viewHolder = XposedHelpers.findClass("androidx.recyclerview.widget.RecyclerView$e0", classLoader);
                adapter = XposedHelpers.findClass("androidx.recyclerview.widget.RecyclerView$h", classLoader);
                hookOnBindViewHolder(classLoader);
                hookCart(classLoader);
            }
        });
    }

    private void hookOnBindViewHolder(ClassLoader classLoader) {
        XposedHelpers.findAndHookMethod(adapter, "bindViewHolder", viewHolder, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Object vh = param.args[0];
                View itemView = (View) XposedHelpers.getObjectField(vh, "itemView");
                XposedBridge.log("Svran:\n VH==> " + vh.getClass().getName() + " , itemView ==> " + itemView);
                Context context = itemView.getContext();
                int versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
                if (itemView instanceof ViewGroup) {
                    ViewGroup vg = (ViewGroup) itemView;
                    longClick(vh, null, vg);
                }
                boolean hooked = false;
                try {
                    Object viewContainer = XposedHelpers.callMethod(vh, "getViewContainer");
                    hookProductCardOrItem(vh, versionCode, viewContainer);
                    hooked = true;
                    XposedBridge.log("Svran: 满足 getViewContainer");
                } catch (NoSuchMethodError e) {
                    XposedBridge.log("Svran: 错误 getViewContainer => " + e.getMessage());
                }
                if (!hooked) try { // 这里应该是购物车的
                    XposedBridge.log("Svran: 购物车 请求方法");
                    String methodName;
                    if (versionCode < 2023144000) methodName = "a0";
                    else if (versionCode < 2023148020) methodName = "b0";
                    else methodName = "c0";
                    // 升级攻略 1 : 查看methodName, 基本上它长度都是2
                    if (debug) {
                        showAllMethod(vh);
                    }
                    Object a0 = XposedHelpers.callMethod(vh, methodName);
                    XposedBridge.log("Svran: a0 : " + a0);
                    hookProductCardOrItem(vh, versionCode, a0);
                } catch (NoSuchMethodError e) {
                    XposedBridge.log("Svran: 错误 购物车 请求方法 => " + e.getMessage());
                }
            }
        });
    }

    private void setOnLongClickAllChildView(Object vh, Object viewContainer, ViewGroup viewGroup, boolean setClickListener) {
        if (viewGroup == null) return;
        StringBuilder builder = new StringBuilder();
        int count = viewGroup.getChildCount();
//        clearAllClickListener(viewGroup);
//        clearAllLongClickListener(viewGroup);
        for (int i = 0; i < count; i++) {
            View child = viewGroup.getChildAt(i);
            if (child != null) builder.append(child.getClass().getName()).append("\n\n");
            if (child instanceof ViewGroup) {
                ViewGroup vgChild = (ViewGroup) child;
//                setOnLongClickAllChildView(vgChild, setClickListener);
                longClick(vh, viewContainer, child);
            } else if (child != null) {
                if (setClickListener) oneClick(child);
                else child.setOnClickListener(null);
            }
        }

        AlertDialog.Builder builder1 = new AlertDialog.Builder(viewGroup.getContext());
        builder1.setTitle("插件: 怎么活啊 - 永辉 - 调试");
        builder1.setMessage(builder.toString());
        builder1.setNegativeButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                printStackTrace();
                dialog.dismiss();
            }
        });
        builder1.show();
    }

    private void clearAllClickListener(ViewGroup group) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View view = group.getChildAt(i);
            if (view instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) view;
                clearAllClickListener(vg);
            } else {
//                view.setOnClickListener(null);
                oneClick(view);
            }
        }
    }

    // 通过id 获取id名称
    private String getViewIdName(View view) {
        return view.getContext().getResources().getResourceEntryName(view.getId());
    }

    // 输出堆栈信息
    private void printStackTrace() {
        try {
//            new Exception().printStackTrace();
            throw new Exception("堆栈信息 Svran Test");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hookCart(ClassLoader classLoader) {
    }

    private void hookProductCardOrItem(Object vh, long versionCode, Object viewContainer) {
//        XposedBridge.log("Svran: 控件: " + viewContainer.getClass().getName());
        switch (viewContainer.getClass().getName()) {
            case "kotlinx.coroutines.internal.j":
            case "":
//                XposedBridge.log("Svran: 跳过 : " + viewContainer.getClass().getName());
                return;
        }

        View image = null;
        View image2 = null;
        TextView title;
        TextView price;
        if (debug) {
            // 升级攻略 2. Log看控件内容
            showAllField(viewContainer);
        }
        Method methodImage = XposedHelpers.findMethodExactIfExists(viewContainer.getClass(), "p");
        Method methodTitle = XposedHelpers.findMethodExactIfExists(viewContainer.getClass(), "z");
        Method methodPrice = XposedHelpers.findMethodExactIfExists(viewContainer.getClass(), "t");

        Field fieldImageI = XposedHelpers.findFieldIfExists(viewContainer.getClass(), "I");
        Field fieldImageK = XposedHelpers.findFieldIfExists(viewContainer.getClass(), "K");
        Field fieldTitle = XposedHelpers.findFieldIfExists(viewContainer.getClass(), "l1");
        Field fieldPrice = XposedHelpers.findFieldIfExists(viewContainer.getClass(), "l");

        if (methodImage != null && methodPrice != null && methodTitle != null) {
            image = (View) XposedHelpers.callMethod(viewContainer, "p");
            image2 = (View) XposedHelpers.callMethod(viewContainer, "p");
            title = (TextView) XposedHelpers.callMethod(viewContainer, "z");
            price = (TextView) XposedHelpers.callMethod(viewContainer, "t");
            if (debug) XposedBridge.log("Svran: 调用方法: " + viewContainer.getClass().getName());
        } else if (fieldTitle != null && fieldPrice != null && (fieldImageI != null || fieldImageK != null)) {
            if (debug)
                XposedBridge.log("Svran: fieldImageI == null : " + (fieldImageI == null) + " , fieldImageK == null : " + (fieldImageK == null));
            try {
                image = (View) XposedHelpers.getObjectField(viewContainer, "I");
            } catch (Throwable e) {
                if (debug) XposedBridge.log("Svran: 图片1错误");
            }
            try {
                image2 = (View) XposedHelpers.getObjectField(viewContainer, "K");
            } catch (Throwable e) {
                if (debug) XposedBridge.log("Svran: 图片2错误");
            }
            if (versionCode < 2023148020)
                title = (TextView) XposedHelpers.getObjectField(viewContainer, "l1");
            else
                title = (TextView) XposedHelpers.getObjectField(viewContainer, "q1");
            price = (TextView) XposedHelpers.getObjectField(viewContainer, "l");
            if (debug) XposedBridge.log("Svran: 获取字段: " + viewContainer.getClass().getName());
        } else {
            title = null;
            price = null;
            if (debug) XposedBridge.log("Svran: 未匹配: " + viewContainer.getClass().getName());
        }
        xpDialog(vh, viewContainer, image, image2, title, price);
    }

    private void xpDialog(Object vh, Object viewContainer, View image, View image2, TextView title, TextView price) {
        if ((image != null || image2 != null) && title != null && price != null) {
            View.OnClickListener l = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setTitle("插件: 怎么活啊 - 永辉");
                    builder.setItems(new CharSequence[]{title.getText() + " , " + price.getText(), "1. 添加到临时商品", "2. 添加商品", "3. 查询 - 比价", "4. 查询 - 比价 - 关键字"}, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            switch (which) {
                                case 1:
                                    intent.setAction(Intent.ACTION_VIEW);
                                    intent.setData(Uri.parse("htl://svran.apps/tmp?name=" + title.getText() + "&price=" + price.getText() + "&mark=来自插件添加"));
                                    v.getContext().startActivity(intent);
                                    break;
                                case 2:
                                    intent.setAction(Intent.ACTION_VIEW);
                                    intent.setData(Uri.parse("htl://svran.apps/add?name=" + title.getText() + "&price=" + price.getText() + "&mark=来自插件添加"));
                                    v.getContext().startActivity(intent);
                                    break;
                                case 3:
                                    intent.setAction(Intent.ACTION_VIEW);
                                    intent.setData(Uri.parse("htl://svran.apps/compare?name=" + title.getText() + "&price=" + price.getText() + "&search=" + getSelectedTextViewText(title)));
                                    v.getContext().startActivity(intent);
                                    break;
                                case 4:
                                    AlertDialog.Builder inputBuilder = new AlertDialog.Builder(v.getContext());
                                    inputBuilder.setTitle("双击选择匹配关键字");
                                    TextView editText = new TextView(v.getContext());
                                    editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                                    editText.setTextIsSelectable(true);
//                                    EditText editText = new EditText(v.getContext());
                                    editText.setText(title.getText());
                                    inputBuilder.setView(editText);
                                    inputBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            intent.setAction(Intent.ACTION_VIEW);
                                            intent.setData(Uri.parse("htl://svran.apps/compare?name=" + title.getText() + "&price=" + price.getText() + "&search=" + getSelectedTextViewText(editText)));
                                            v.getContext().startActivity(intent);
                                        }
                                    });
                                    inputBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    });
                                    inputBuilder.show();
                                    break;
                                default:
                            }
                        }
                    });
                    builder.show();
                }
            };
            if (image != null) image.setOnClickListener(l);
            if (image2 != null) image2.setOnClickListener(l);
            longClick(vh, viewContainer, image);
            longClick(vh, viewContainer, image2);
        }
    }

    private void longClick(Object vh, Object viewContainer, View view) {
        if (view != null) view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("插件: 怎么活啊 - 永辉 - 调试");
                builder.setItems(new CharSequence[]{"获取父控件所有控件", "给父控件下所有子控件设置点击事件", "Method列表, vh名: " + vh.getClass().getName(), "Filed 列表"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
//                            setOnLongClickAllChildView()
                            setOnLongClickAllChildView(vh, viewContainer, (ViewGroup) v.getParent(), false);
                        } else if (which == 1) {
//                            findAndShowAllViews
                            clearAllClickListener((ViewGroup) v.getParent());
                            setOnLongClickAllChildView(vh, viewContainer, (ViewGroup) v.getParent(), true);
                        } else if (which == 2) {
                            showMessageDialog(view.getContext(), "vh", showAllMethod(vh));
                        } else {
                            if (viewContainer == null)
                                Toast.makeText(v.getContext(), "没有 viewContainer", Toast.LENGTH_LONG).show();
                            else
                                showMessageDialog(view.getContext(), "viewContainer", showAllField(viewContainer));
                        }
                    }
                });
                builder.show();
                return true;
            }
        });
    }

    private void showMessageDialog(Context context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title == null ? "消息" : title);
        builder.setMessage(message);
        builder.setPositiveButton("确定", null);
        builder.show();
    }

    private void oneClick(View view) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogShowInfo(v.getContext(), "one点击到了: " + v.getClass().getName() + "\n\nID: " + getViewIdName(v));
            }
        });
    }

    private String getSelectedTextViewText(TextView textView) {
        if (textView == null) {
            return "";
        } else {
            CharSequence text = textView.getText();
            if (textView.getSelectionStart() == textView.getSelectionEnd()) {
                return text.toString();
            } else {
                CharSequence selected = text.subSequence(textView.getSelectionStart(), textView.getSelectionEnd());
                return selected.toString();
            }
        }
    }

    private void dialogShowInfo(Context context, String message) {
        XposedBridge.log("SvranDialogShowInfo: " + message);
//        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("插件: 怎么活啊 - 永辉 - 调试");
        builder.setMessage(message);
        builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                printStackTrace();
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void testHook(ClassLoader classLoader) {
    }

    private String showAllMethod(Object vh) {
        StringBuilder sbd = new StringBuilder();
        // 获取vh所有method
        for (Method method : vh.getClass().getMethods()) {
            String rt = method.getReturnType().getName();
            int pts = method.getParameterTypes().length;
            if (!Objects.equals(rt, "void") && pts == 0 && method.getName().length() <= 3) {
                String logText = "Svran: name:" + method.getName() + " , 返回类型:" + rt + " , 参数个数: " + pts;
                XposedBridge.log(logText);
                sbd.append(logText).append('\n');
            }
        }
        return sbd.toString();
    }

    private String showAllField(Object viewContainer) {
        StringBuilder sbd = new StringBuilder();
        // 获取所有字段 展示
        for (Field field : viewContainer.getClass().getDeclaredFields()) {
            Object f = XposedHelpers.getObjectField(viewContainer, field.getName());
            if (f instanceof TextView) {
                TextView textView = (TextView) f;
                String txt = textView.getText().toString();
                if (txt != null && !txt.isEmpty()) {
                    String logText = "Svran: 字段: " + field.getName() + " , 获取字段值: " + txt;
                    XposedBridge.log(logText);
                    sbd.append(logText).append('\n');
                }
            }
        }
        return sbd.toString();
    }
}
