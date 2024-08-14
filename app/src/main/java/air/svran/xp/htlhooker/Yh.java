package air.svran.xp.htlhooker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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
//                hookRvSetAdapter(classLoader);
//                hookRvCreateVh(classLoader);
//                hookOnBindViewHolder(classLoader);
                hookRvVhOnBind(classLoader);
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
                int itemViewType = 0;
                itemViewType = (int) XposedHelpers.callMethod(vh, "getItemViewType");
                switch (vh.getClass().getName()) {
                    case "com.dmall.cms.adapter.NavigationRecyclerViewAdapter$ItemViewHolder":
                        break;
                    case "com.dmall.trade.dto.cart.viewbinder.CartCommonWareViewBinder$CartCommonWareViewHolder":
                        break;
                    case "com.dmall.framework.views.recyclerview.ItemViewHolder":
                        break;
                    case "com.dmall.order.orderdetail.OrderWaresView$MyViewHolder":
                        break;
                    case "com.dmall.trade.dto.cart.viewbinder.CartRecommendViewBinder$CartRecommendViewHolder":
                        break;
                    default:
//                        XposedBridge.log("Svran: 绑定bindViewHolder(" + itemViewType + "):" + param.args[0].getClass().getName());
                        break;
                }
            }
        });
    }

    private void hookRvVhOnBind(ClassLoader classLoader) {
        XposedHelpers.findAndHookMethod(adapter, "bindViewHolder", viewHolder, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                XposedBridge.log("Svran: 绑定bindViewHolder:" + param.args[0].getClass().getName());
                try {
                    Method method = XposedHelpers.findMethodBestMatch(param.args[0].getClass(), "getViewContainer");
                    if (method != null) {
                        try {
                            Object viewContainer = XposedHelpers.callMethod(param.args[0], "getViewContainer");
                            // 此处获取的可能是 ma.a
                            hookProductCardOrItem(viewContainer);
//                            hookTestViewContainer(viewContainer);
                        } catch (Exception e) {
//                            XposedBridge.log("Svran: getViewContainer => " + e.getMessage());
                        }
                    }
                } catch (NoSuchMethodError e) {
//                    XposedBridge.log("Svran: ma.a => " + e.getMessage());
                }
                try {
                    Method method = XposedHelpers.findMethodBestMatch(param.args[0].getClass(), "a0");
                    if (method != null) {
                        try {
                            Object a0 = XposedHelpers.callMethod(param.args[0], "a0");
                            // 此处获取的可能是 hc.o3
                            hookProductCardOrItem(a0);
//                            hookTestA0(a0);
                        } catch (Exception e) {
//                            XposedBridge.log("Svran: a0 => " + e.getMessage());
                        }
                    }
                } catch (NoSuchMethodError e) {
//                    XposedBridge.log("Svran: hc.o3 => " + e.getMessage());
                }
            }
        });
    }

    private void hookTestA0(Object a0) {
        XposedBridge.log("Svran: 购物车类型 -> a0:" + a0.getClass().getName());
//        findViewField(a0, "A"); // FrameLayout
//        findViewField(a0, "B"); // RelativeLayout
//        findViewField(a0, "C"); // FlexboxLayout
        findViewField(a0, "D"); // PriceFontView
//        findViewField(a0, "E"); // IconFont
//        findViewField(a0, "F"); // IconFont
//        findViewField(a0, "G"); // IconFont
//        findViewField(a0, "H"); // IconFont
//        findViewField(a0, "I"); // RoundImageLoaderView // 商品图
        findViewField(a0, "J"); // RoundImageLoaderView
//        findViewField(a0, "K"); // TextView // 售罄商品图上的售罄文字
//        findViewField(a0, "L"); // RelativeLayout
//        findViewField(a0, "M"); // LinearLayout
//        findViewField(a0, "N"); // LinearLayout
//        findViewField(a0, "O"); // IconFont
        findViewField(a0, "P"); // PriceFontView
//        findViewField(a0, "Q"); // ConstraintLayout
//        findViewField(a0, "R"); // Space
//        findViewField(a0, "S"); // RoundConstraintLayout
//        findViewField(a0, "T"); // RoundTextView
//        findViewField(a0, "U"); // CartCountDownView
//            public final f3 V;
//        findViewField(a0, "V");
//            public final f3 W;
//        findViewField(a0, "W");
//            public final f3 X;
//        findViewField(a0, "X");
//        findViewField(a0, "Y"); // TextView
//        findViewField(a0, "Z"); // DraweeTextView
//        findViewField(a0, "a"); // RoundConstraintLayout
//        findViewField(a0, "b"); // LinearLayout
//        findViewField(a0, "c"); // Space
//        findViewField(a0, "d"); // DraweeTextView
//        findViewField(a0, "e"); // TextView
//        findViewField(a0, "e1"); // Space
        findViewField(a0, "f"); // ImageView
        findViewField(a0, "f1"); // PriceFontView
//        findViewField(a0, "g"); // ConstraintLayout
//        findViewField(a0, "g1"); // TextView
//        findViewField(a0, "h"); // LinearLayout
//        findViewField(a0, "h1"); // SubmitButton
//        findViewField(a0, "i"); // YHCheckBox
//        findViewField(a0, "i1"); // TextView
//        findViewField(a0, "j"); // ConstraintLayout
//        findViewField(a0, "j1"); // TextView
//        findViewField(a0, "k"); // IconFont
//        findViewField(a0, "k1"); // TextView
//        findViewField(a0, "l"); // 现价格 // PriceFontView
//        findViewField(a0, "l1"); // 标题 商品名等 // DraweeTextView
//        findViewField(a0, "m"); // DraweeTextView
//        findViewField(a0, "m1"); // DraweeTextView
//        findViewField(a0, "n"); // DraweeTextView
//        findViewField(a0, "n1"); // DraweeTextView
//        findViewField(a0, "o"); // TextView
//        findViewField(a0, "o1"); // TextView
//        findViewField(a0, "p"); // TextView
//        findViewField(a0, "p1"); // DraweeTextView
//        findViewField(a0, "q"); // DraweeTextView
//        findViewField(a0, "q1"); // DraweeTextView
//        findViewField(a0, "r"); // DraweeTextView
//        findViewField(a0, "r1"); // DraweeTextView
//        findViewField(a0, "s"); // DraweeTextView
//        findViewField(a0, "s1"); // DraweeTextView
//        findViewField(a0, "t"); // ConstraintLayout f60784t;
//        findViewField(a0, "t1"); // TextView
//        findViewField(a0, "u"); // ConstraintLayout f60786u;
//        findViewField(a0, "u1"); // SubmitButton
//        findViewField(a0, "v"); // LinearLayout f60788v;
//        findViewField(a0, "v1"); // TextView
//        findViewField(a0, "w"); // TextView f60790w;
//        findViewField(a0, "w1"); // TextView
        findViewField(a0, "x"); // PriceFontView f60792x;
//        findViewField(a0, "x1"); // View
//        findViewField(a0, "y"); // ConstraintLayout f60794y;
//        findViewField(a0, "y1"); // View
//        findViewField(a0, "z"); // FrameLayout f60796z;
    }

    private void hookTestViewContainer(Object viewContainer) {
        XposedBridge.log("Svran: 大卡片类型 -> viewContainer:" + viewContainer.getClass().getName());
        findViewMethod(viewContainer, "B");
        findViewMethod(viewContainer, "D");
//        findViewMethod(viewContainer, "E"); // x件起购
        findViewMethod(viewContainer, "F");
        findViewMethod(viewContainer, "G");
//        findViewMethod(viewContainer, "H"); // 折券 / 搜索商品: 热销榜
        findViewMethod(viewContainer, "I");
        findViewMethod(viewContainer, "J");
//        findViewMethod(viewContainer, "K"); // 布局
//        findViewMethod(viewContainer, "b"); // 到货提醒
//        findViewMethod(viewContainer, "c"); // 加入购物车
        findViewMethod(viewContainer, "d");
        findViewMethod(viewContainer, "e");
        findViewMethod(viewContainer, "f");
        findViewMethod(viewContainer, "g");
        findViewMethod(viewContainer, "h");
        findViewMethod(viewContainer, "i");
//        findViewMethod(viewContainer, "j"); // 限时抢 进度条
        findViewMethod(viewContainer, "k");
        findViewMethod(viewContainer, "l");
        findViewMethod(viewContainer, "m");
        findViewMethod(viewContainer, "n");
        findViewMethod(viewContainer, "o");
//        findViewMethod(viewContainer, "p"); // 商品图片
//        findViewMethod(viewContainer, "q"); // 买过 / xx人认为xxx
//        findViewMethod(viewContainer, "r"); // 原价
        findViewMethod(viewContainer, "s");
//        findViewMethod(viewContainer, "t"); // 现价
//        findViewMethod(viewContainer, "u"); // x.x折特价
        findViewMethod(viewContainer, "v");
//        findViewMethod(viewContainer, "w"); // 描述
        findViewMethod(viewContainer, "x");
//        findViewMethod(viewContainer, "y"); // 折券
//        findViewMethod(viewContainer, "z"); // 商品 标题 土豆 xxg
    }

//    private void hookRvCreateVh(ClassLoader classLoader) {
//        XposedHelpers.findAndHookMethod(viewHolder, "createViewHolder", android.view.ViewGroup.class, int.class, new XC_MethodHook() {
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                super.beforeHookedMethod(param);
//            }
//
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                super.afterHookedMethod(param);
//                XposedBridge.log("Svran: 创建createViewHolder:" + param.thisObject.getClass().getName());
//            }
//        });
//    }
//
//    private void hookRvSetAdapter(ClassLoader classLoader) {
//        XposedHelpers.findAndHookMethod("androidx.recyclerview.widget.RecyclerView", classLoader, "setAdapter", viewHolder, new XC_MethodHook() {
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                super.beforeHookedMethod(param);
//            }
//
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                super.afterHookedMethod(param);
//                if (param.args[0] != null)
//                    XposedBridge.log("Svran: 设置setAdapter:" + param.args[0].getClass().getName());
//            }
//        });
//    }

    private void hookProductCardOrItem(Object viewContainer) {
        View image = null;
        TextView title;
        TextView price;
        switch (viewContainer.getClass().getName()) {
            case "ma.a":
                image = (View) XposedHelpers.callMethod(viewContainer, "p");
                title = (TextView) XposedHelpers.callMethod(viewContainer, "z");
                price = (TextView) XposedHelpers.callMethod(viewContainer, "t");
                break;
            case "hc.o3":
                try {
                    image = (View) XposedHelpers.getObjectField(viewContainer, "I");
                } catch (Exception e) {
                }
                if (image == null) try {
                    image = (View) XposedHelpers.getObjectField(viewContainer, "K");
                } catch (Exception e) {
                }
                title = (TextView) XposedHelpers.getObjectField(viewContainer, "l1");
                price = (TextView) XposedHelpers.getObjectField(viewContainer, "l");
                break;
            default:
                XposedBridge.log("Svran: 控件: " + viewContainer.getClass().getName());
                image = (View) XposedHelpers.callMethod(viewContainer, "p");
                title = (TextView) XposedHelpers.callMethod(viewContainer, "z");
                price = (TextView) XposedHelpers.callMethod(viewContainer, "t");
        }

        if (image != null && title != null && price != null) {
            image.setOnClickListener(new View.OnClickListener() {
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
            });
            longClick(image);
        }
    }

    private void longClick(View view) {
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("插件: 怎么活啊 - 永辉 - 调试");
                builder.setItems(new CharSequence[]{"获取父控件所有控件", "给父控件下所有子控件设置点击事件"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            findAndShowAllViews((ViewGroup) v.getParent(), false);
                        } else if (which == 1) {
                            findAndShowAllViews((ViewGroup) v.getParent(), true);
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
                Toast.makeText(v.getContext(), "one点击到了: " + v.getClass().getName(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 获取父控件所有控件, Dialog输出
    private void findAndShowAllViews(ViewGroup viewGroup, boolean setClickListener) {
        if (viewGroup == null) {
            return;
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (child != null) {
                if (setClickListener) {
                    oneClick(child);
                    longClick(child);
                }
                builder.append(child.getClass().getName()).append("\n\n");
            }
        }
        AlertDialog.Builder builder1 = new AlertDialog.Builder(viewGroup.getContext());
        builder1.setTitle("插件: 怎么活啊 - 永辉 - 调试");
        builder1.setMessage(builder.toString());
        builder1.setNegativeButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder1.show();
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

    private void findViewMethod(Object viewContainer, String name) {
        View l = null;
        try {
            l = (View) XposedHelpers.callMethod(viewContainer, name);
        } catch (Exception e) {
            XposedBridge.log("Svran: " + name + " 错误 =>" + e.getMessage());
        }
        if (l != null) {
            XposedBridge.log("Svran: 单项Method: （" + name + "） -> " + l);
            l.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Toast.makeText(v.getContext(), "长按到了" + name, Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
            l.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(), "点击到了" + name, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void findViewField(Object viewContainer, String name) {
        View l = null;
        try {
            l = (View) XposedHelpers.getObjectField(viewContainer, name);
        } catch (NoSuchMethodError | ClassCastException | NoSuchFieldError e) {
            XposedBridge.log("Svran: " + name + " 错误 =>" + e.getMessage());
        }
        if (l != null) {
            XposedBridge.log("Svran: 单项Field: （" + name + "） -> " + l);
            l.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    XposedBridge.log("Svran: 长按到了 " + name);
                    Toast.makeText(v.getContext(), "长按到了" + name, Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
            l.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    XposedBridge.log("Svran: 点击到了 " + name);
                    Toast.makeText(v.getContext(), "点击到了" + name, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void testHook(ClassLoader classLoader) {
    }
}
