package org.zywx.wbpalmstar.engine.webview;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Message;
import android.view.KeyEvent;
import android.webkit.ValueCallback;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xwalk.core.XWalkJavascriptResult;
import org.xwalk.core.XWalkUIClient;
import org.xwalk.core.XWalkView;
import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.base.WebViewSdkCompat;
import org.zywx.wbpalmstar.engine.EBrowserActivity;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.EBrowserWindow;
import org.zywx.wbpalmstar.engine.ELinkedList;
import org.zywx.wbpalmstar.engine.ESystemInfo;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;
import org.zywx.wbpalmstar.engine.universalex.EUExManager;
import org.zywx.wbpalmstar.engine.universalex.EUExScript;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.engine.universalex.ThirdPluginObject;

import java.util.Map;

public class CBrowserMainFrame extends XWalkUIClient {
    protected String mParms;
    protected String mReferenceUrl;
    private boolean mIsPageOnload;

    public CBrowserMainFrame(XWalkView view) {
        super(view);
        mReferenceUrl = "";
    }

    @Override
    public boolean onConsoleMessage(XWalkView view, String message,
                                    int lineNumber, String sourceId, ConsoleMessageType messageType) {
        return super.onConsoleMessage(view, message, lineNumber, sourceId,
                messageType);
    }

    @Override
    public boolean onCreateWindowRequested(XWalkView view,
                                           InitiateBy initiator, ValueCallback<XWalkView> callback) {
        return super.onCreateWindowRequested(view, initiator, callback);
    }

    @Override
    public void onFullscreenToggled(XWalkView view, boolean enterFullscreen) {
        super.onFullscreenToggled(view, enterFullscreen);
    }

    @Override
    public void onIconAvailable(XWalkView view, String url,
                                Message startDownload) {
        super.onIconAvailable(view, url, startDownload);
    }

    @Override
    public void onJavascriptCloseWindow(XWalkView view) {
        super.onJavascriptCloseWindow(view);
    }

    @Override
    public boolean onJavascriptModalDialog(XWalkView view,
                                           JavascriptMessageType type, String url, String message,
                                           String defaultValue, final XWalkJavascriptResult result) {
        if (type == JavascriptMessageType.JAVASCRIPT_PROMPT) {
            return onJsPrompt(view, url, message, defaultValue, result);
        } else if (type == JavascriptMessageType.JAVASCRIPT_ALERT) {
            return onJsAlert(view, url, message, result);
        } else if (type == JavascriptMessageType.JAVASCRIPT_CONFIRM) {
            return onJsConfirm(view, url, message, result);
        } else {
            return true;
        }
    }

    @Override
    public void onPageLoadStarted(XWalkView view, String url) {
        mIsPageOnload = false;
        BDebug.i("url ", url);
        if (view == null) {
            return;
        }
        EBrowserView target = (EBrowserView) view;
        target.onPageStarted(target, url);
        if (null != mParms) {
            target.setQuery(mParms);
        }
        mParms = null;
        ESystemInfo info = ESystemInfo.getIntence();
        if (info.mFinished) {
            info.mScaled = true;
        }
        if (url != null) {
            mReferenceUrl = url;
            if (url.startsWith("http")) {
                EBrowserWindow bWindow = target.getBrowserWindow();
                if (bWindow != null && 1 == bWindow.getWidget().m_webapp) {
                    bWindow.showProgress();
                }
            }
        }
    }

    @Override
    public void onPageLoadStopped(XWalkView view, String url, LoadStatus status) {
        BDebug.i("url ", url, status);
        if (status == LoadStatus.FINISHED) {
            if (view == null) {
                return;
            }
            EBrowserView target = (EBrowserView) view;
            EBrowserWindow bWindow = target.getBrowserWindow();
            if (url != null) {
                if (url.startsWith("http")) {
                    if (bWindow != null && 1 == bWindow.getWidget().m_webapp) {
                        bWindow.hiddenProgress();
                    }
                }
                String oUrl = view.getOriginalUrl();
                if (!mReferenceUrl.equals(url) || target.beDestroy()
                        || !url.equals(oUrl) && mIsPageOnload) {
                    return;
                }
            }
            mIsPageOnload = true;
            ESystemInfo info = ESystemInfo.getIntence();

            int versionA = Build.VERSION.SDK_INT;

            if (!target.isWebApp()) { // 4.3及4.3以下手机
                if (!info.mScaled) {
                    float nowScale = 1.0f;

//					if (versionA <= 18) {
                    nowScale = target.getScale();
//					}

                    info.mDefaultFontSize = (int) (info.mDefaultFontSize / nowScale);
                    info.mScaled = true;

                }

//				target.setDefaultFontSize(48);
            }
            if (!info.mFinished) {
                ((EBrowserActivity) target.getContext())
                        .setContentViewVisible(200);
            }

            info.mFinished = true;
            target.loadUrl(EUExScript.F_UEX_DISPATCHER_SCRIPT);
            target.loadUrl(EUExScript.F_UEX_SCRIPT);
            target.onPageFinished(target, url);
            if (bWindow != null && bWindow.getWidget().m_appdebug == 1) {
                String debugUrlString = "http://"
                        + bWindow.getWidget().m_logServerIp
                        + ":30060/target/target-script-min.js#anonymous";
                String weinreString = "javascript:var x = document.createElement(\"SCRIPT\");x.setAttribute('src',\""
                        + debugUrlString
                        + "\""
                        + ");document.body.appendChild(x);";
                target.loadUrl(weinreString);
            }

            BDebug.i(url, "   loaded");
        }

    }

    @Override
    public void onReceivedIcon(XWalkView view, String url, Bitmap icon) {
        super.onReceivedIcon(view, url, icon);
    }

    @Override
    public void onReceivedTitle(XWalkView view, String title) {
        super.onReceivedTitle(view, title);
    }

    @Override
    public void onRequestFocus(XWalkView view) {
        super.onRequestFocus(view);
    }

    @Override
    public void onScaleChanged(XWalkView view, float oldScale, float newScale) {
        super.onScaleChanged(view, oldScale, newScale);
    }

    @Override
    public void onUnhandledKeyEvent(XWalkView view, KeyEvent event) {
        super.onUnhandledKeyEvent(view, event);
    }

    @Override
    public void openFileChooser(XWalkView view, ValueCallback<Uri> uploadFile,
                                String acceptType, String capture) {
        ((EBrowserActivity) view.getContext()).setmUploadMessage(getCompatCallback(uploadFile));
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        ((EBrowserActivity) view.getContext()).startActivityForResult(
                Intent.createChooser(i, "File Chooser"),
                EBrowserActivity.FILECHOOSER_RESULTCODE);
    }

    @Override
    public boolean onJsPrompt(XWalkView view, String url, String message, String defaultValue, final XWalkJavascriptResult result) {
        if (message != null
                && message.startsWith(EUExScript.JS_APPCAN_ONJSPARSE)) {
            appCanJsParse(result, view,
                    message.substring(EUExScript.JS_APPCAN_ONJSPARSE.length()));
            result.cancel();
        } else {
            if (!((EBrowserActivity) view.getContext()).isVisable()) {
                result.cancel();
                return true;
            }
            AlertDialog.Builder dia = new AlertDialog.Builder(view.getContext());
            dia.setTitle(null);
            dia.setMessage(message);
            final EditText input = new EditText(view.getContext());
            if (defaultValue != null) {
                input.setText(defaultValue);
            }
            input.setSelectAllOnFocus(true);
            dia.setView(input);
            dia.setCancelable(false);
            dia.setPositiveButton(EUExUtil.getResStringID("confirm"),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            result.confirmWithResult(input.getText().toString());
                        }
                    });
            dia.setNegativeButton(EUExUtil.getResStringID("cancel"),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            result.cancel();
                        }
                    });
            dia.create();
            dia.show();
        }
        return true;
    }

    @Override
    public boolean onJsAlert(XWalkView view, String url, String message, XWalkJavascriptResult result) {
        return super.onJsAlert(view, url, message, result);
    }

    @Override
    public boolean onJsConfirm(XWalkView view, String url, String message, XWalkJavascriptResult result) {
        return super.onJsConfirm(view, url, message, result);
    }

    @Override
    public boolean shouldOverrideKeyEvent(XWalkView view, KeyEvent event) {
        // TODO Auto-generated method stub
        return super.shouldOverrideKeyEvent(view, event);
    }

    private void appCanJsParse(final XWalkJavascriptResult result, XWalkView view, String parseStr) {
        try {
            if (!(view instanceof EBrowserView)) {
                return;
            }
            JSONObject json = new JSONObject(parseStr);
            String uexName = json.optString("uexName");
            String method = json.optString("method");
            JSONArray jsonArray = json.getJSONArray("args");
            JSONArray typesArray = json.getJSONArray("types");
            int length = jsonArray.length();
            String[] args = new String[length];
            for (int i = 0; i < length; i++) {
                String type = typesArray.getString(i);
                String arg = jsonArray.getString(i);
                if ("undefined".equals(type) && "null".equals(arg)) {
                    args[i] = null;
                } else {
                    args[i] = arg;
                }
            }
            EBrowserView browserView = (EBrowserView) view;
            final EUExManager uexManager = browserView.getEUExManager();
            if (uexManager != null) {
                BDebug.i("appCanJsParse", "dispatch parseStr " + parseStr);
                ELinkedList<EUExBase> plugins = uexManager
                        .getThirdPlugins();
                for (EUExBase plugin : plugins) {
                    if (plugin.getUexName().equals(uexName)) {
                        Object object = uexManager.callMethod(plugin,
                                method, args);
                        if (null != object) {
                            result.confirmWithResult(object.toString());
                            return;
                        }
                    }
                }
                // 调用单实例插件
                Map<String, ThirdPluginObject> thirdPlugins = uexManager
                        .getPlugins();
                ThirdPluginObject thirdPluginObject = thirdPlugins
                        .get(uexName);
                if (thirdPluginObject != null
                        && thirdPluginObject.isGlobal
                        && thirdPluginObject.pluginObj != null) {
                    Object object = uexManager.callMethod(
                            thirdPluginObject.pluginObj,
                            method, args);
                    if (null != object) {
                        result.confirmWithResult(object.toString());
                        return;
                    }
                }
                BDebug.e("plugin", uexName, "not exist...");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public WebViewSdkCompat.ValueCallback<Uri> getCompatCallback(final ValueCallback<Uri> uploadMsg){
        return new WebViewSdkCompat.ValueCallback<Uri>() {
            @Override
            public void onReceiveValue(Uri uri) {
                uploadMsg.onReceiveValue(uri);
            }
        };
    }

}
