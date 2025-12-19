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
                boolean hooked = false;
                try {
                    Object viewContainer = XposedHelpers.callMethod(vh, "getViewContainer");
                    XposedBridge.log("Svran: 满足 getViewContainer");
                    hookProductItem(vh, versionCode, viewContainer);
                    longClick(vh, viewContainer, itemView);
                    hooked = true;
                } catch (NoSuchMethodError e) {
                    XposedBridge.log("Svran: 错误 getViewContainer => " + e.getMessage());
                }
                if (!hooked) try { // 这里应该是购物车的
                    XposedBridge.log("Svran: 购物车 请求方法");
                    String methodName = "f0";
                    // 升级攻略 1 : 查看methodName, 基本上它长度都是2
                    if (debug) {
                        showAllMethod(vh);
                    }
                    Method method = XposedHelpers.findMethodExactIfExists(vh.getClass(), methodName);
                    if (getViewIdName(itemView).equals("root_layout") || method.getReturnType().getName().equals("view")) {
                        Object a0 = XposedHelpers.callMethod(vh, methodName);
                        XposedBridge.log("Svran: a0 : " + a0);
                        hookCartItem(vh, versionCode, a0);
                        longClick(vh, a0, itemView);
                    }
                } catch (NoSuchMethodError e) {
                    XposedBridge.log("Svran: 错误 购物车 请求方法 => " + e.getMessage());
                    for (Method method : vh.getClass().getMethods()) {
                        String rt = method.getReturnType().getName();
                        if (rt.length() == 5 && rt.indexOf(2) == '.') {
                            try {
                                Object a0 = XposedHelpers.callMethod(vh, rt);
                                XposedBridge.log("Svran矫正: a0 : " + a0);
                                hookCartItem(vh, versionCode, a0);
                                longClick(vh, a0, itemView);
                            } catch (NoSuchMethodError e2) {
                                XposedBridge.log("Svran: 错误 购物车 升级方法 => " + e2.getMessage());
                            }
                            break;
                        }
                    }
                }
            }
        });
    }

    private void setOnLongClickAllChildView(Object vh, Object viewContainer, ViewGroup viewGroup, boolean setClickListener) {
        if (viewGroup == null) return;
        StringBuilder builder = new StringBuilder();
        int count = viewGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = viewGroup.getChildAt(i);
            if (child != null) builder.append(child.getClass().getName()).append("\n\n");
            if (child instanceof ViewGroup) {
                ViewGroup vgChild = (ViewGroup) child;
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
                oneClick(view);
            }
        }
    }

    // 通过id 获取id名称
    private String getViewIdName(View view) {
        if (view.getId() == -1) return "View.NO_ID";
        return view.getContext().getResources().getResourceEntryName(view.getId());
    }

    // 输出堆栈信息
    private void printStackTrace() {
        try {
            throw new Exception("堆栈信息 Svran Test");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hookCart(ClassLoader classLoader) {
    }

    private void hookProductItem(Object vh, long versionCode, Object viewContainer) {
//        XposedBridge.log("Svran: 控件: " + viewContainer.getClass().getName());
        switch (viewContainer.getClass().getName()) {
            case "kotlinx.coroutines.internal.j":
            case "":
//                XposedBridge.log("Svran: 跳过 : " + viewContainer.getClass().getName());
                return;
        }

        if (debug) {
            // 升级攻略 2. Log看控件内容
            showAllField(viewContainer);
        }

        String img1FieldName = "d";
        String img2FieldName = "img2";
        String titleFieldName = "e";
        String priceFieldName = "i";
        Field image1Field = XposedHelpers.findFieldIfExists(viewContainer.getClass(), img1FieldName);
        Field image2Field = XposedHelpers.findFieldIfExists(viewContainer.getClass(), img2FieldName);
        Field titleField = XposedHelpers.findFieldIfExists(viewContainer.getClass(), titleFieldName);
        Field priceField = XposedHelpers.findFieldIfExists(viewContainer.getClass(), priceFieldName);
        View image1 = null;
        View image2 = null;
        TextView title = null;
        TextView price = null;
        if (image1Field != null) {
            Object imgTmp = XposedHelpers.getObjectField(viewContainer, img1FieldName);
            image1 = imgTmp instanceof View ? (View) imgTmp : null;
        }
        if (image2Field != null) {
            Object imgTmp = XposedHelpers.getObjectField(viewContainer, img2FieldName);
            image2 = imgTmp instanceof View ? (View) imgTmp : null;
        }
        if (titleField != null) {
            Object titleTmp = XposedHelpers.getObjectField(viewContainer, titleFieldName);
            title = titleTmp instanceof TextView ? (TextView) titleTmp : null;
        }
        if (priceField != null) {
            Object priceTmp = XposedHelpers.getObjectField(viewContainer, priceFieldName);
            price = priceTmp instanceof TextView ? (TextView) priceTmp : null;
        }
        xpDialog(vh, viewContainer, image1, image2, title, price);
        if ((image2Field != null || image1Field != null) && titleField != null && priceField != null) {
            XposedBridge.log("Svran: 获取成功");
        } else {
            XposedBridge.log("Svran: 没有获取到");
        }
    }

    private void hookCartItem(Object vh, long versionCode, Object viewContainer) {
//        XposedBridge.log("Svran: 控件: " + viewContainer.getClass().getName());
        switch (viewContainer.getClass().getName()) {
            case "kotlinx.coroutines.internal.j":
            case "":
//                XposedBridge.log("Svran: 跳过 : " + viewContainer.getClass().getName());
                return;
        }

        if (debug) {
            // 升级攻略 2. Log看控件内容
            showAllField(viewContainer);
        }

        String img1FieldName = "N";
        String img2FieldName = "img2";
        String titleFieldName = "v1";
        String priceFieldName = "l";
        Field image1Field = XposedHelpers.findFieldIfExists(viewContainer.getClass(), img1FieldName);
        Field image2Field = XposedHelpers.findFieldIfExists(viewContainer.getClass(), img2FieldName);
        Field titleField = XposedHelpers.findFieldIfExists(viewContainer.getClass(), titleFieldName);
        Field priceField = XposedHelpers.findFieldIfExists(viewContainer.getClass(), priceFieldName);
        View image1 = null;
        View image2 = null;
        TextView title = null;
        TextView price = null;
        if (image1Field != null) {
            Object imgTmp = XposedHelpers.getObjectField(viewContainer, img1FieldName);
            image1 = imgTmp instanceof View ? (View) imgTmp : null;
        }
        if (image2Field != null) {
            Object imgTmp = XposedHelpers.getObjectField(viewContainer, img2FieldName);
            image2 = imgTmp instanceof View ? (View) imgTmp : null;
        }
        if (titleField != null) {
            Object titleTmp = XposedHelpers.getObjectField(viewContainer, titleFieldName);
            title = titleTmp instanceof TextView ? (TextView) titleTmp : null;
        }
        if (priceField != null) {
            Object priceTmp = XposedHelpers.getObjectField(viewContainer, priceFieldName);
            price = priceTmp instanceof TextView ? (TextView) priceTmp : null;
        }
        xpDialog(vh, viewContainer, image1, image2, title, price);
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
                            setOnLongClickAllChildView(vh, viewContainer, (ViewGroup) v.getParent(), false);
                        } else if (which == 1) {
                            clearAllClickListener((ViewGroup) v.getParent());
                            Toast.makeText(v.getContext(), "设置完成", Toast.LENGTH_SHORT).show();
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
//            if (!Objects.equals(rt, "void") && pts == 0 && method.getName().length() <= 3) {
            String logText = "★名:" + method.getName() + " , 返:" + rt + " , 参(个): " + pts;
            XposedBridge.log(logText);
            sbd.append(logText).append('\n');
//            }
        }
        String sbdText = sbd.toString();
        XposedBridge.log("Svran: showAllMethod: " + sbdText);
        return sbdText;
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
                    String logText = "★名: " + field.getName() + " , 文: " + txt;
                    XposedBridge.log(logText);
                    sbd.append(logText).append('\n');
                }
            } else if (f instanceof View) {
                View view = (View) f;
                if (view.isShown()) {
                    String logText = "★名: " + field.getName() + " , " + (view.isShown() ? "显" : "隐") + "ID: " + getViewIdName(view) + "类: " + view.getClass().getName();
                    XposedBridge.log(logText);
                    sbd.append(logText).append('\n');
                }
            } else {
                String logText = "★名: " + field.getName() + " , 值: " + f;
                XposedBridge.log(logText);
                sbd.append(logText).append('\n');
            }
        }
        String sbdText = sbd.toString();
        XposedBridge.log("Svran: showAllField: " + sbdText);
        return sbdText;
    }
}
