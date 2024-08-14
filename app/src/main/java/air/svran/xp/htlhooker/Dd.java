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
public class Dd {
    public Dd(XC_LoadPackage.LoadPackageParam lpparam) {
        hook(lpparam);
    }

    public void hook(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedBridge.log("Svran: Hook 多点");
        hookDd(lpparam.classLoader);
    }

    private void hookDd(ClassLoader classLoader) {
        String appClassName = "com.wrapper.proxyapplication.WrapperProxyApplication";
        boolean canHook = SvranHookUtils.findMethod(appClassName, "onCreate", classLoader, "hook 支持");
        if (!canHook) {
            XposedBridge.log("Svran: 不可hook 支持");
            return;
        }

        XposedHelpers.findAndHookMethod(appClassName, classLoader, "onCreate", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                XposedBridge.log("Svran: 进入多点hook");
                viewHolder = XposedHelpers.findClass("androidx.recyclerview.widget.RecyclerView$ViewHolder", classLoader);
                adapter = XposedHelpers.findClass("androidx.recyclerview.widget.RecyclerView$Adapter", classLoader);
                hookOnBindViewHolder(classLoader);
//                hookCreateViewHolder(classLoader);
            }
        });
    }

    private void hookCreateViewHolder(ClassLoader classLoader) {
        XposedHelpers.findAndHookMethod(adapter, "createViewHolder", ViewGroup.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                XposedBridge.log("Svran: 创建createViewHolder:" + param.getResult().getClass().getName());
            }
        });
    }

    private Class<?> viewHolder;
    private Class<?> adapter;

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
                        homeItem(vh, itemViewType);
                        break;
                    case "com.dmall.trade.dto.cart.viewbinder.CartCommonWareViewBinder$CartCommonWareViewHolder":
                        cartItem(vh, itemViewType);
                        break;
                    case "com.dmall.framework.views.recyclerview.ItemViewHolder":
                        searchItem(vh, itemViewType);
                        break;
                    case "com.dmall.order.orderdetail.OrderWaresView$MyViewHolder":
                        orderItem(vh, itemViewType);
                        break;
                    case "com.dmall.trade.dto.cart.viewbinder.CartRecommendViewBinder$CartRecommendViewHolder":
                        recommendItem(vh, itemViewType);
                        break;
                    default:
//                        XposedBridge.log("Svran: 绑定bindViewHolder(" + itemViewType + "):" + param.args[0].getClass().getName());
                        break;
                }
            }
        });
    }

    private void recommendItem(Object vh, int itemViewType) {
        try {
            Object itemView = XposedHelpers.getObjectField(vh, "itemView"); // GoodsItemViewForTwoColumn
            Object mGoodItemBottomView = XposedHelpers.getObjectField(itemView, "mGoodItemBottomView");
            Object mGoodsItemPriceView = XposedHelpers.getObjectField(mGoodItemBottomView, "mGoodsItemPriceView");
            TextView mDisplayPriceTextView = (TextView) XposedHelpers.getObjectField(mGoodsItemPriceView, "mDisplayPriceTextView");
            Object mGoodsHeadView = XposedHelpers.getObjectField(itemView, "mGoodsHeadView");
            Object mSquareTagsImageView = XposedHelpers.getObjectField(mGoodsHeadView, "mSquareTagsImageView");
            Object mGoodsItemMiddleView = XposedHelpers.getObjectField(itemView, "mGoodsItemMiddleView");
            Object mWareNameTextView = XposedHelpers.getObjectField(mGoodsItemMiddleView, "mWareNameTextView");
            if (mSquareTagsImageView instanceof View && mWareNameTextView instanceof TextView && mDisplayPriceTextView instanceof TextView) {
                hookProductCardOrItem((View) mSquareTagsImageView, (TextView) mWareNameTextView, (TextView) mDisplayPriceTextView);
            } else {
                boolean b1 = mSquareTagsImageView instanceof View;
                boolean b2 = mWareNameTextView instanceof TextView;
                boolean b3 = mDisplayPriceTextView instanceof TextView;
                XposedBridge.log("Svran: " + b1 + " " + b2 + " " + b3);
            }
        } catch (Exception e) {
            XposedBridge.log("SvranE recommend: " + e.getMessage());
        }
    }

    private void orderItem(Object vh, int itemViewType) {
        try {
            switch (itemViewType) {
                case 0: // com.dmall.category.views.item.SearchItemWare1NView
                    Object binding = XposedHelpers.getObjectField(vh, "binding"); // GoodsItemViewForTwoColumn
                    XposedBridge.log("Svran: itemView :" + binding.getClass().getName());
                    Object imageView = XposedHelpers.getObjectField(binding, "imageView");
                    Object tvWareName = XposedHelpers.getObjectField(binding, "tvWareName");
                    Object tvWarePrice = XposedHelpers.getObjectField(binding, "tvWarePrice");
                    if (imageView instanceof View && tvWareName instanceof TextView && tvWarePrice instanceof TextView) {
                        hookProductCardOrItem((View) imageView, (TextView) tvWareName, (TextView) tvWarePrice);
                    } else {
                        boolean b1 = imageView instanceof View;
                        boolean b2 = tvWareName instanceof TextView;
                        boolean b3 = tvWarePrice instanceof TextView;
                        XposedBridge.log("Svran: " + b1 + " " + b2 + " " + b3);
                    }
                    break;
            }
        } catch (Exception e) {
            XposedBridge.log("SvranE order: " + e.getMessage());
        }
    }

    private void searchItem(Object vh, int itemViewType) {
        try {
            switch (itemViewType) {
                case 100: // com.dmall.category.views.item.SearchItemWare1NView
                    Object itemView = XposedHelpers.getObjectField(vh, "itemView"); // GoodsItemViewForTwoColumn
//                    XposedBridge.log("Svran: itemView :" + itemView.getClass().getName());
                    Object pictureIV = XposedHelpers.getObjectField(itemView, "pictureIV");
                    Object nameTV = XposedHelpers.getObjectField(itemView, "nameTV");
                    Object priceTV = XposedHelpers.getObjectField(itemView, "priceTV");
                    if (pictureIV instanceof View && nameTV instanceof TextView && priceTV instanceof TextView) {
                        hookProductCardOrItem((View) pictureIV, (TextView) nameTV, (TextView) priceTV);
                    } else {
                        boolean b1 = pictureIV instanceof View;
                        boolean b2 = nameTV instanceof TextView;
                        boolean b3 = priceTV instanceof TextView;
//                        XposedBridge.log("Svran: " + b1 + " " + b2 + " " + b3);
                    }
                    break;
            }
        } catch (Exception e) {
            XposedBridge.log("SvranE search: " + e.getMessage());
        }
    }

    private void cartItem(Object vh, int itemViewType) {
        try {
            switch (itemViewType) {
                case 7:
                    Object cartCommonWareView = XposedHelpers.getObjectField(vh, "cartCommonWareView"); // CartCommonWareView
                    Object mPictureIV = XposedHelpers.getObjectField(cartCommonWareView, "mPictureIV");
                    Object mCartPriceView = XposedHelpers.getObjectField(cartCommonWareView, "mCartPriceView");
                    Object mPriceTextView = XposedHelpers.getObjectField(mCartPriceView, "mPriceTextView");
                    Object mNameTV = XposedHelpers.getObjectField(cartCommonWareView, "mNameTV");
                    if (mPictureIV instanceof View && mNameTV instanceof TextView && mPriceTextView instanceof TextView) {
                        hookProductCardOrItem((View) mPictureIV, (TextView) mNameTV, (TextView) mPriceTextView);
                    } else {
                        boolean b1 = mPictureIV instanceof View;
                        boolean b2 = mNameTV instanceof TextView;
                        boolean b3 = mPriceTextView instanceof TextView;
//                        XposedBridge.log("Svran: " + b1 + " " + b2 + " " + b3);
                    }
                    break;
            }
        } catch (Exception e) {
            XposedBridge.log("SvranE cart: " + e.getMessage());
        }
    }

    private void homeItem(Object vh, int itemViewType) {
        try {
            switch (itemViewType) {
                case 21:
                    Object itemView = XposedHelpers.getObjectField(vh, "itemView"); // GoodsItemViewForTwoColumn
                    Object mGoodItemBottomView = XposedHelpers.getObjectField(itemView, "mGoodItemBottomView");
                    Object mGoodsItemPriceView = XposedHelpers.getObjectField(mGoodItemBottomView, "mGoodsItemPriceView");
                    TextView mDisplayPriceTextView = (TextView) XposedHelpers.getObjectField(mGoodsItemPriceView, "mDisplayPriceTextView");
                    Object mGoodsHeadView = XposedHelpers.getObjectField(itemView, "mGoodsHeadView");
                    Object mSquareTagsImageView = XposedHelpers.getObjectField(mGoodsHeadView, "mSquareTagsImageView");
                    Object mGoodsItemMiddleView = XposedHelpers.getObjectField(itemView, "mGoodsItemMiddleView");
                    Object mWareNameTextView = XposedHelpers.getObjectField(mGoodsItemMiddleView, "mWareNameTextView");
                    if (mSquareTagsImageView instanceof View && mWareNameTextView instanceof TextView && mDisplayPriceTextView instanceof TextView) {
                        hookProductCardOrItem((View) mSquareTagsImageView, (TextView) mWareNameTextView, (TextView) mDisplayPriceTextView);
                    } else {
                        boolean b1 = mSquareTagsImageView instanceof View;
                        boolean b2 = mWareNameTextView instanceof TextView;
                        boolean b3 = mDisplayPriceTextView instanceof TextView;
//                        XposedBridge.log("Svran: " + b1 + " " + b2 + " " + b3);
                    }
                    break;
                default:
            }
        } catch (Exception e) {
            XposedBridge.log("SvranE home: " + e.getMessage());
        }
    }

    private void hookProductCardOrItem(View image, TextView title, TextView price) {
        if (image != null && title != null && price != null) {
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setTitle("插件: 怎么活啊 - 重百");
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
                builder.setTitle("插件: 怎么活啊 - 重百 - 调试");
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
        builder1.setTitle("插件: 怎么活啊 - 重百 - 调试");
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
