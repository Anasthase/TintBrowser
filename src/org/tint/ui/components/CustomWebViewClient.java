package org.tint.ui.components;

import org.tint.R;
import org.tint.ui.UIManager;
import org.tint.utils.ApplicationUtils;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebView.HitTestResult;
import android.widget.EditText;

public class CustomWebViewClient extends WebViewClient {

	private UIManager mUIManager;
	
	private Message mDontResend;
    private Message mResend;
	
	public CustomWebViewClient(UIManager uiManager) {
		mUIManager = uiManager;
	}
	
	@Override
	public void onPageStarted(WebView view, String url, Bitmap favicon) {
		mUIManager.onPageStarted(view, url, favicon);
		((CustomWebView) view).onClientPageStarted(url);
	}

	@Override
	public void onPageFinished(WebView view, String url) {
		mUIManager.onPageFinished(view, url);
		((CustomWebView) view).onClientPageFinished(url);		
	}

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		if (view.getHitTestResult().getType() == HitTestResult.EMAIL_TYPE) {
			Intent sendMail = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			mUIManager.getMainActivity().startActivity(sendMail);
			return true;
		}
		
		return false;
	}

	@Override
	public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(view.getResources().getString(R.string.SslWarningsHeader));
		sb.append("\n\n");
		
		if (error.hasError(SslError.SSL_UNTRUSTED)) {
			sb.append(" - ");
			sb.append(view.getResources().getString(R.string.SslUntrusted));
			sb.append("\n");
		}
		
		if (error.hasError(SslError.SSL_IDMISMATCH)) {
			sb.append(" - ");
			sb.append(view.getResources().getString(R.string.SslIDMismatch));
			sb.append("\n");
		}
		
		if (error.hasError(SslError.SSL_EXPIRED)) {
			sb.append(" - ");
			sb.append(view.getResources().getString(R.string.SslExpired));
			sb.append("\n");
		}
		
		if (error.hasError(SslError.SSL_NOTYETVALID)) {
			sb.append(" - ");
			sb.append(view.getResources().getString(R.string.SslNotYetValid));
			sb.append("\n");
		}
		
		ApplicationUtils.showContinueCancelDialog(view.getContext(),
				android.R.drawable.ic_dialog_info,
				view.getResources().getString(R.string.SslWarning),
				sb.toString(),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						handler.proceed();
					}

				},
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						handler.cancel();
					}
		});
	}
	
	@Override
	public void onReceivedHttpAuthRequest(WebView view, final HttpAuthHandler handler, final String host, final String realm) {
		String username = null;
        String password = null;
        
        boolean reuseHttpAuthUsernamePassword = handler.useHttpAuthUsernamePassword();
        
        if (reuseHttpAuthUsernamePassword && view != null) {
            String[] credentials = view.getHttpAuthUsernamePassword(
                    host, realm);
            if (credentials != null && credentials.length == 2) {
                username = credentials[0];
                password = credentials[1];
            }
        }

        if (username != null && password != null) {
            handler.proceed(username, password);
        } else {
        	LayoutInflater factory = LayoutInflater.from(mUIManager.getMainActivity());
            final View v = factory.inflate(R.layout.http_authentication_dialog, null);
            
            if (username != null) {
                ((EditText) v.findViewById(R.id.username_edit)).setText(username);
            }
            if (password != null) {
                ((EditText) v.findViewById(R.id.password_edit)).setText(password);
            }
            
            AlertDialog dialog = new AlertDialog.Builder(mUIManager.getMainActivity())
            .setTitle(String.format(mUIManager.getMainActivity().getString(R.string.HttpAuthenticationDialogDialogTitle), host, realm))
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setView(v)
            .setPositiveButton(R.string.Proceed,
                    new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog,
                                 int whichButton) {
                            String nm = ((EditText) v
                                    .findViewById(R.id.username_edit))
                                    .getText().toString();
                            String pw = ((EditText) v
                                    .findViewById(R.id.password_edit))
                                    .getText().toString();
                            mUIManager.setHttpAuthUsernamePassword(host, realm, nm, pw);
                            handler.proceed(nm, pw);
                        }})
            .setNegativeButton(R.string.Cancel,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            handler.cancel();
                        }})
            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        handler.cancel();
                    }})
            .create();
            
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            dialog.show();
                        
            v.findViewById(R.id.username_edit).requestFocus();            
        }
	}

	@Override
	public void onFormResubmission(WebView view, Message dontResend, Message resend) {
		
		mDontResend = dontResend;
		mResend = resend;
		
		new AlertDialog.Builder(mUIManager.getMainActivity()).setTitle(R.string.FormResubmitTitle)
			.setMessage(R.string.FormResubmitMessage)
                .setPositiveButton(R.string.OK,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                if (mResend != null) {
                                	mResend.sendToTarget();
                                	mResend = null;
                                	mDontResend = null;
                                }
                            }
                        }).setNegativeButton(R.string.Cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                if (mDontResend != null) {
                                	mDontResend.sendToTarget();
                                	mResend = null;
                                	mDontResend = null;
                                }
                            }
                        }).setOnCancelListener(new OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        if (mDontResend != null) {
                        	mDontResend.sendToTarget();
                        	mResend = null;
                        	mDontResend = null;
                        }
                    }
                }).show();
	}	
}
