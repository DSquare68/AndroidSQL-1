package com.example.myapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;


/**
 * Activity for SELECT operation.
 */
public class SelectActivity extends AppCompatActivity {

    /* Constants */

    public static final String OK_LABEL = "Ok";
    public static final String PLEASE_WAIT_LABEL = "Please Wait.";
    public static final String EXCEPTION_LABEL = "Exception: ";

    /* State */

    private Button doSelectSelectQueryButton;
    private EditText selectQueryEditText;
    private ProgressDialog progressDialog;
    private ListView productsListView;

    private Button returnButton;

    private String exceptionMessageString = "";

    private AlertDialog errorAlertDialog;

    private boolean busy = false;

    private Context context;
    private View view;


    /* Accessors */

    public Button getDoSelectSelectQueryButton() {
        return doSelectSelectQueryButton;
    }

    public void setDoSelectSelectQueryButton(final Button doSelectSelectQueryButton) {
        this.doSelectSelectQueryButton = doSelectSelectQueryButton;
    }

    public EditText getSelectQueryEditText() {
        return selectQueryEditText;
    }

    public void setSelectQueryEditText(final EditText selectQueryEditText) {
        this.selectQueryEditText = selectQueryEditText;
    }

    public ProgressDialog getProgressDialog() {
        return progressDialog;
    }

    public void setProgressDialog(final ProgressDialog progressDialog) {
        this.progressDialog = progressDialog;
    }

    public ListView getProductsListView() {
        return productsListView;
    }

    public void setProductsListView(final ListView productsListView) {
        this.productsListView = productsListView;
    }

    public Button getReturnButton() {
        return returnButton;
    }

    public void setReturnButton(final Button returnButton) {
        this.returnButton = returnButton;
    }

    public String getExceptionMessageString() {
        return exceptionMessageString;
    }

    public void setExceptionMessageString(final String exceptionMessageString) {
        this.exceptionMessageString = exceptionMessageString;
    }

    public AlertDialog getErrorAlertDialog() {
        return errorAlertDialog;
    }

    public void setErrorAlertDialog(final AlertDialog errorAlertDialog) {
        this.errorAlertDialog = errorAlertDialog;
    }

    public boolean isBusy() {
        return busy;
    }

    public void setBusy(final boolean busy) {
        this.busy = busy;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(final Context context) {
        this.context = context;
    }

    public View getView() {
        return view;
    }

    public void setView(final View view) {
        this.view = view;
    }


    /* Methods */

    /**
     * @author Andrzej Wysocki (nm108).
     */
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContext(this);
        prepareView();
    }

    /**
     * @author Andrzej Wysocki (nm108).
     */
    private void prepareView() {
        setContentView(R.layout.activity_select);
        setDoSelectSelectQueryButton(findViewById(R.id.DoSelectQueryButton));
        setSelectQueryEditText(findViewById(R.id.SelectQueryEditText));
        setProductsListView(findViewById(R.id.ProductsListView));
        setReturnButton(findViewById(R.id.ReturnButton));

        getDoSelectSelectQueryButton().setOnClickListener(
                this::selectQueryClickHandler
        );

        returnButton.setOnClickListener(
                (final View v) -> {
                    switchActivityToMain();
                }
        );

        prepareProgressDialog();
        prepareErrorAlertDialog();
    }

    /**
     * @author Andrzej Wysocki (nm108).
     */
    private void prepareProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage(PLEASE_WAIT_LABEL);
    }

    /**
     * @author Andrzej Wysocki (nm108).
     */
    private void switchActivityToMain() {
        final Intent switchActivityIntent = new Intent(this, MainActivity.class);
        startActivity(switchActivityIntent);
    }

    /**
     * @author Andrzej Wysocki (nm108).
     */
    public void selectQueryClickHandler(final View v) {
        // we do not want button clicks queueing, so we just 'swallow'
        // unneeded extra ones.
        if (isBusy()) {
            return;
        }
        setBusy(true);

        // informing user that we are busy
        progressDialog.show();

        // querying Database (SELECT Operation).
        try {
            final SqlSelectAsyncTask sTask = new SqlSelectAsyncTask();
            sTask.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // to be continued when async task calls us back.
    }

    /**
     * GUI preparation.
     *
     * @author Andrzej Wysocki (nm108).
     * */
    private void prepareErrorAlertDialog() {
        setErrorAlertDialog(
                new AlertDialog.Builder(this).create());
        errorAlertDialog.setTitle(EXCEPTION_LABEL);
        errorAlertDialog.setCancelable(false);
        errorAlertDialog.setCanceledOnTouchOutside(false);
        errorAlertDialog.setButton(
                AlertDialog.BUTTON_NEUTRAL, (CharSequence) OK_LABEL,
                (DialogInterface.OnClickListener) (dialog, which) ->
                {
                    dialog.dismiss();
                    switchActivityToMain();
                });
    }


    /**
     * Async Task for querying database in background (SELECT operation)
     *
     * @author Andrzej Wysocki (nm108).
     */
    class SqlSelectAsyncTask extends AsyncTask<List[], Void, ArrayList<Product>> {
        private boolean error = false;

        public boolean isError() {
            return error;
        }

        public void setError(boolean error) {
            this.error = error;
        }

        /**
         * @author Andrzej Wysocki (nm108).
         */
        public ArrayList<Product> doInBackground(List[]... params) {
            ArrayList<Product> result = null;

            final JDBCDatabaseHelper jdbcDatabaseHelper = new JDBCDatabaseHelper(context);
            try {
                result = jdbcDatabaseHelper.doSelect(selectQueryEditText.getText().toString());
            } catch (Exception e) {
                setError(true);
                exceptionMessageString = e.toString();
                return null;
            }
            return result;
        }

        @Override
        /**
         * @author Andrzej Wysocki (nm108).
         */
        protected void onPostExecute(ArrayList<Product> products) {
            super.onPostExecute(products);

            // handling errors
            if (isError()) {
                errorAlertDialog.setMessage(EXCEPTION_LABEL+exceptionMessageString);
                errorAlertDialog.show();
                setBusy(false);
                setError(false);
                progressDialog.dismiss();
                return;
            }

            // populating Products List View
            final ProductAdapter pa = new ProductAdapter(context, products);
            getProductsListView().setAdapter(pa);

            // work done, cleaning up
            setBusy(false);
            progressDialog.dismiss();
        }
    }
}