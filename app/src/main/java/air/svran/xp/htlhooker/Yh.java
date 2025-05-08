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
//                XposedBridge.log("Svran:\n VH==> " + vh.getClass().getName() + " , itemView ==> " + itemView);
                if (itemView instanceof ViewGroup) {
                    ViewGroup vg = (ViewGroup) itemView;
                    longClick(vg);
                }
                boolean hooked = false;
                try {
                    Object viewContainer = XposedHelpers.callMethod(vh, "getViewContainer");
                    hookProductCardOrItem(viewContainer);
                    hooked = true;
//                    XposedBridge.log("Svran: 满足 getViewContainer");
                } catch (NoSuchMethodError e) {
//                    XposedBridge.log("Svran: 错误 getViewContainer => " + e.getMessage());
                }
                if (!hooked) try { // 这里应该是购物车的
//                    XposedBridge.log("Svran: 购物车 请求方法");
                    Context context = itemView.getContext();
                    int versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
                    String methodName = (versionCode < 2023144000) ? "a0" : "b0";

                    Object a0 = XposedHelpers.callMethod(vh, methodName);
                    hookProductCardOrItem(a0);
                } catch (NoSuchMethodError e) {
//                    XposedBridge.log("Svran: 错误 购物车 请求方法 => " + e.getMessage());
                }
            }
        });
    }

    private void setOnLongClickAllChildView(ViewGroup viewGroup, boolean setClickListener) {
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
                longClick(child);
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

    private void hookProductCardOrItem(Object viewContainer) {
        View image = null;
        View image2 = null;
        TextView title;
        TextView price;
        // 升级攻略 1. 获取控件
//        XposedBridge.log("Svran: 控件: " + viewContainer.getClass().getName());
        // 升级攻略 2. Log看控件名
        switch (viewContainer.getClass().getName()) {
            case "kotlinx.coroutines.internal.j":
            case "":
//                XposedBridge.log("Svran: 跳过 : " + viewContainer.getClass().getName());
                return;
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
//            XposedBridge.log("Svran: 调用方法: " + viewContainer.getClass().getName());
        } else if (fieldTitle != null && fieldPrice != null && (fieldImageI != null || fieldImageK != null)) {
//            XposedBridge.log("Svran: fieldImageI == null : " + (fieldImageI == null) + " , fieldImageK == null : " + (fieldImageK == null));
            try {
                image = (View) XposedHelpers.getObjectField(viewContainer, "I");
            } catch (Throwable e) {
            }
            try {
                image2 = (View) XposedHelpers.getObjectField(viewContainer, "K");
            } catch (Throwable e) {
            }
            title = (TextView) XposedHelpers.getObjectField(viewContainer, "l1");
            price = (TextView) XposedHelpers.getObjectField(viewContainer, "l");
//            XposedBridge.log("Svran: 获取字段: " + viewContainer.getClass().getName());
        } else {
            title = null;
            price = null;
//            XposedBridge.log("Svran: 未匹配: " + viewContainer.getClass().getName());
        }
        xpDialog(image, image2, title, price);
    }

    private void xpDialog(View image, View image2, TextView title, TextView price) {
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
            longClick(image);
            longClick(image2);
        }
    }

    private void longClick(View view) {
        if (view != null) view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("插件: 怎么活啊 - 永辉 - 调试");
                builder.setItems(new CharSequence[]{"获取父控件所有控件", "给父控件下所有子控件设置点击事件"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
//                            setOnLongClickAllChildView()
                            setOnLongClickAllChildView((ViewGroup) v.getParent(), false);
                        } else if (which == 1) {
//                            findAndShowAllViews
                            clearAllClickListener((ViewGroup) v.getParent());
                            setOnLongClickAllChildView((ViewGroup) v.getParent(), true);
                        }
                    }
                });
                builder.show();
                return true;
            }
        });
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
}
