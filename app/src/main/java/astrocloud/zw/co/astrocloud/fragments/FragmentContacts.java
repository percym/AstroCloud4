package astrocloud.zw.co.astrocloud.fragments;


import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.appolica.flubber.Flubber;
import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeErrorDialog;
import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeInfoDialog;
import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeSuccessDialog;
import com.awesomedialog.blennersilva.awesomedialoglibrary.interfaces.Closure;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import net.ralphpina.permissionsmanager.PermissionsManager;
import net.ralphpina.permissionsmanager.PermissionsResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import astrocloud.zw.co.astrocloud.App;
import astrocloud.zw.co.astrocloud.R;
import astrocloud.zw.co.astrocloud.UploadActivity;
import astrocloud.zw.co.astrocloud.adapters.ContactsAdapter;
import astrocloud.zw.co.astrocloud.models.ContactModel;
import astrocloud.zw.co.astrocloud.utils.GLOBALDECLARATIONS;
import de.hdodenhof.circleimageview.CircleImageView;
import rx.functions.Action1;


/**
 * Created by Percy M on 11/30/2017.
 */

public class FragmentContacts extends Fragment {

    RecyclerView contactsRecyclerView;
    RelativeLayout folderStateContainer;
    View view;
    private static final String TAG = FragmentContacts.class.getCanonicalName();
    private ArrayList<Object> arrayListContacts = new ArrayList<>();
    private ArrayList<ContactModel> arrayListContactsToBeWritten = new ArrayList<>();
    private ArrayList<ContactModel> arrayListContactsToDisplay;
    private DatabaseReference contactsDatabase;
    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    DatabaseReference contactsChildReference;
    Query myContactsQuery;
    ContactsAdapter adapter;
    GenericTypeIndicator<HashMap<String, Object>> genericTypeIndicator = new GenericTypeIndicator<HashMap<String, Object>>();
    AwesomeInfoDialog awesomeInfoDialog;
    AwesomeInfoDialog awesomeErrorDialog;
    CircleImageView emptyfolder, imageUploadContacts;
    RelativeLayout folder_state_container;
    private Paint p = new Paint();
    String name, phonenumber;
    private DividerItemDecoration mDividerItemDecoration;
    Handler h = new Handler();
    int delay = 15000; //15 seconds
    Runnable runnable;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_contacts, container, false);
        contactsRecyclerView = view.findViewById(R.id.contactsRecyclerView);
        emptyfolder = view.findViewById(R.id.empty_folder_icon);
        folder_state_container = view.findViewById(R.id.folder_state_container);
        imageUploadContacts = view.findViewById(R.id.imageUploadContacts);
        imageUploadContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (PermissionsManager.get().isContactsGranted()) {

                    awesomeErrorDialog = new AwesomeInfoDialog(getContext());
                    awesomeErrorDialog
                            .setTitle(R.string.app_name)
                            .setMessage(" Do you want to upload your contacts to your cloud account ?")
                            .setDialogIconOnly(R.drawable.ic_app_icon)
                            .setColoredCircle(R.color.white)
                            .setCancelable(false)
                            .setPositiveButtonText(getString(R.string.upload))
                            .setPositiveButtonbackgroundColor(R.color.dialogSuccessBackgroundColor)
                            .setPositiveButtonTextColor(R.color.white)
                            .setNegativeButtonText(getString(R.string.cancel))
                            .setNegativeButtonbackgroundColor(R.color.dialogErrorBackgroundColor)
                            .setNegativeButtonTextColor(R.color.white)
                            .setPositiveButtonClick(new Closure() {
                                @Override
                                public void exec() {
                                    writecontacts();
                                }
                            })
                            .setNegativeButtonClick(new Closure() {
                                @Override
                                public void exec() {

                                }
                            })
                            .show();

                } else if (PermissionsManager.get().neverAskForContacts(getActivity())) {

                    showPermissionsDialogue();
                } else {
                    PermissionsManager.get().requestContactsPermission()
                            .subscribe(new Action1<PermissionsResult>() {
                                @Override
                                public void call(PermissionsResult permissionsResult) {
                                    if (!permissionsResult.isGranted()) {
                                        showPermissionsDialogue();

                                    } else {
                                        writecontacts();

                                    }
                                }
                            });


                }

            }
        });
        contactsDatabase = FirebaseDatabase.getInstance().getReference();
        contactsChildReference = contactsDatabase.child("contacts");


        showFetchcontactsDialogue();
        arrayListContactsToDisplay = new ArrayList<>();
        getContacts();
        adapter = new ContactsAdapter(getContext(), arrayListContactsToDisplay);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        contactsRecyclerView.setLayoutManager(mLayoutManager);
        contactsRecyclerView.setItemAnimator(new DefaultItemAnimator());
//        mDividerItemDecoration = new DividerItemDecoration(
//                container.getContext(),1
//        );
//        contactsRecyclerView.addItemDecoration(mDividerItemDecoration);

        contactsRecyclerView.setAdapter(adapter);
//        contactsRecyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getContext()).color(Color.parseColor("#4bde90")).sizeResId(R.dimen.divider).marginResId(R.dimen.leftmargin, R.dimen.rightmargin).build());

        initSwipe();
        refreshView();
        awesomeInfoDialog.hide();
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        h.removeCallbacks(runnable); //stop handler when activity not visible
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        super.onResume();

        h.postDelayed(new Runnable() {
            public void run() {
                refreshView();

                runnable=this;

                h.postDelayed(runnable, delay);
            }
        }, delay);
    }

    public static FragmentContacts newInstance(int val) {
        FragmentContacts fragmentContacts = new FragmentContacts();
        // Supply val input as an argument.
        Bundle args = new Bundle();
        args.putInt("val", val);
        fragmentContacts.setArguments(args);
        return fragmentContacts;
    }

    private ArrayList<Object> getContacts() {
        arrayListContacts = new ArrayList<>();
        arrayListContactsToDisplay = new ArrayList<>();
        myContactsQuery = contactsDatabase.child("contacts").child(userId);
        myContactsQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                fillArrayListWithContactsDB(dataSnapshot);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                //fillArrayListWithContactsDBRemoved(dataSnapshot);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                String databaseErrorString = databaseError.getDetails();
                showFetchErrorDialogue(databaseErrorString);

            }
        });
//        myContactsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                fillArrayListWithContactsDB(dataSnapshot);
//               contactsRecyclerView.invalidate(); adapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                String databaseErrorString = databaseError.getDetails().toString();
//                showFetchErrorDialogue(databaseErrorString);
//
//            }
//        });
        awesomeInfoDialog.hide();
        GLOBALDECLARATIONS.GLOBAL_CONTACTS_ARRAYLIST = arrayListContactsToDisplay;
        return arrayListContacts;

    }

    private void fillArrayListWithContactsDB(DataSnapshot dataSnapshot) {
            System.out.println(dataSnapshot.getValue());

            //   Log.e(TAG, ds.toString());
        Object randomMap = dataSnapshot.getValue();
        Map<String, Map<String, String>> contactsDataMap = (Map<String, Map<String, String>>)randomMap;
        Object numberObject = contactsDataMap.get("number");
        Object nameObject = contactsDataMap.get("name");
        String number = (String) numberObject;
        String name = (String) nameObject;//
        if(name == null){
            name = "No Name";
        }
        if(number == null){
            number = "No Number";
        }





//            ContactModel contactModel = dataSnapshot.getValue(ContactModel.class) ;
          ContactModel contactModel = new ContactModel(name, number) ;


                arrayListContactsToDisplay.add(contactModel);

        adapter.notifyDataSetChanged();

    }

    public void showUploadingcontactsDialogue() {
        awesomeInfoDialog = new AwesomeInfoDialog(getActivity());
        awesomeInfoDialog
                .setTitle(R.string.app_name)
                .setMessage("Uploading your contacts")
                .setDialogIconOnly(R.drawable.ic_app_icon)
                .setColoredCircle(R.color.white)
                .setCancelable(false)
                .show();

    }
    public void showFetchcontactsDialogue() {
        awesomeInfoDialog = new AwesomeInfoDialog(getActivity());
        awesomeInfoDialog
                .setTitle(R.string.app_name)
                .setMessage("Uploading your contacts")
                .setDialogIconOnly(R.drawable.ic_app_icon)
                .setColoredCircle(R.color.white)
                .setCancelable(false)
                .show();

    }
    public void showFetchErrorDialogue(String databaseErrorString) {

        new AwesomeErrorDialog(getActivity())
                .setTitle(R.string.app_name)
                .setMessage(databaseErrorString)
                .setDialogIconOnly(R.drawable.ic_app_icon)
                .setCancelable(true).setButtonText(getString(R.string.dialog_ok_button))
                .setButtonBackgroundColor(R.color.dialogErrorBackgroundColor)
                .setButtonText("Close")
                .setErrorButtonClick(new Closure() {
                    @Override
                    public void exec() {
                        // click
                    }
                })
                .show();
    }

    private void initSwipe() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();

                if (direction == ItemTouchHelper.RIGHT) {
//                    removeView();

                    name = ((TextView) contactsRecyclerView.findViewHolderForAdapterPosition(position).itemView.findViewById(R.id.firstLine)).getText().toString();
                    phonenumber = ((TextView) contactsRecyclerView.findViewHolderForAdapterPosition(position).itemView.findViewById(R.id.secondLine)).getText().toString();
                    if (PermissionsManager.get().isContactsGranted()) {
                        if (doesContactExist(name)) {
                            awesomeErrorDialog = new AwesomeInfoDialog(getContext());
                            awesomeErrorDialog
                                    .setTitle(R.string.app_name)
                                    .setMessage("Contact " + name + " already exists. Do you want to continue restoring this contact ?")
                                    .setDialogIconOnly(R.drawable.ic_app_icon)
                                    .setColoredCircle(R.color.white)
                                    .setCancelable(false)
                                    .setPositiveButtonText(getString(R.string.restore))
                                    .setPositiveButtonbackgroundColor(R.color.dialogSuccessBackgroundColor)
                                    .setPositiveButtonTextColor(R.color.white)
                                    .setNegativeButtonText(getString(R.string.cancel))
                                    .setNegativeButtonbackgroundColor(R.color.dialogErrorBackgroundColor)
                                    .setNegativeButtonTextColor(R.color.white)
                                    .setPositiveButtonClick(new Closure() {
                                        @Override
                                        public void exec() {
                                            addContactToPhone(name, phonenumber);
                                            adapter.notifyDataSetChanged();
                                        }
                                    })
                                    .setNegativeButtonClick(new Closure() {
                                        @Override
                                        public void exec() {
                                            adapter.notifyDataSetChanged();

                                        }
                                    })
                                    .show();
                        } else {
                            addContactToPhone(name, phonenumber);
                            contactsRecyclerView.invalidate();
                            adapter.notifyDataSetChanged();
                        }

//                    edit_position = position;
//                    alertDialog.setTitle("Edit Country");
//                    et_country.setText(countries.get(position));
//                    alertDialog.show();
                        //               } else {

                        //                  adapter.removeItem(position);
                    } else if (PermissionsManager.get().neverAskForContacts(FragmentContacts.this)) {

                        showFetchcontactsDialogue();
                    } else {
                        PermissionsManager.get().requestContactsPermission()
                                .subscribe(new Action1<PermissionsResult>() {
                                    @Override
                                    public void call(PermissionsResult permissionsResult) {
                                        if (!permissionsResult.isGranted()) {
                                            showPermissionsDialogue();

                                        } else {
                                            addContactToPhone(name, phonenumber);

                                        }
                                    }
                                });


                    }
                }

            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                Bitmap icon;
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

                    View itemView = viewHolder.itemView;
                    float height = (float) itemView.getBottom() - (float) itemView.getTop();
                    float width = height / 3;

                    if (dX > 0) {
                        p.setColor(Color.parseColor("#096d39"));
                        RectF background = new RectF((float) itemView.getLeft(), (float) itemView.getTop(), dX, (float) itemView.getBottom());
                        c.drawRect(background, p);
                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.cloud_computing);
                        RectF icon_dest = new RectF((float) itemView.getLeft() + width, (float) itemView.getTop() + width, (float) itemView.getLeft() + 2 * width, (float) itemView.getBottom() - width);
                        c.drawBitmap(icon, null, icon_dest, p);
//                    } else {
//                        p.setColor(Color.parseColor("#D32F2F"));
//                        RectF background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(),(float) itemView.getRight(), (float) itemView.getBottom());
//                        c.drawRect(background,p);
//                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_delete_white);
//                        RectF icon_dest = new RectF((float) itemView.getRight() - 2*width ,(float) itemView.getTop() + width,(float) itemView.getRight() - width,(float)itemView.getBottom() - width);
//                        c.drawBitmap(icon,null,icon_dest,p);
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(contactsRecyclerView);
    }

    private void addContactToPhone(String name, String number) {

        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        intent.putExtra(ContactsContract.Intents.Insert.NAME, name);
        intent.putExtra(ContactsContract.Intents.Insert.PHONE, number);
        startActivity(intent);
    }

    private void removeView() {
        if (view.getParent() != null) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
    }

    private void restoreView() {

        if (view.getParent() != null) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
    }

    private boolean doesContactExist(String contactName) {
        String contactNumber = "";
        boolean state = false;
        Uri uri = ContactsContract.Data.CONTENT_URI;
        String[] projection = new String[]{ContactsContract.PhoneLookup._ID};
        String selection = ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME + " = ?";
        String[] selectionArguments = {contactName};
        Cursor cursor = getActivity().getContentResolver().query(uri, projection, selection, selectionArguments, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                contactNumber = cursor.getString(0);
                Log.d("the #", contactNumber);
                state = true;
            }
        } else {
            state = false;
        }
        return state;
    }

    public void showUploadcontactsDialogue(String contactName) {
        awesomeInfoDialog = new AwesomeInfoDialog(getContext());
        awesomeInfoDialog
                .setTitle(R.string.app_name)
                .setMessage("Contact " + contactName + " already exists. Do you want to continue restoring this contact ?")
                .setDialogIconOnly(R.drawable.ic_app_icon)
                .setColoredCircle(R.color.white)
                .setCancelable(false)
                .setPositiveButtonText("Restore")
                .setNegativeButtonText("Cancel")
                .setPositiveButtonClick(new Closure() {
                    @Override
                    public void exec() {


                    }
                })
                .setNegativeButtonClick(new Closure() {
                    @Override
                    public void exec() {
                        contactsRecyclerView.invalidate();
                        adapter.notifyDataSetChanged();
                    }
                })
                .show();

    }

    private void snackShower(String message) {
        Snackbar snackbar;
        snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(getResources().getColor(R.color.mainGreen));
        TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        snackbar.show();
    }

    public void showPermissionsDialogue() {
        new AwesomeSuccessDialog(getActivity())
                .setTitle("AstroCloud")
                .setMessage("Permission to read and write contacts is needed for application to function properly")
                .setColoredCircle(R.color.white)
                .setDialogIconOnly(R.drawable.ic_app_icon)
                .setCancelable(false)
                .setPositiveButtonText(getString(R.string.give_permissions))
                .setPositiveButtonbackgroundColor(R.color.dialogSuccessBackgroundColor)
                .setPositiveButtonTextColor(R.color.white)
                .setNegativeButtonText(getString(R.string.dialog_no_button))
                .setNegativeButtonbackgroundColor(R.color.dialogErrorBackgroundColor)
                .setNegativeButtonTextColor(R.color.white)
                .setPositiveButtonClick(new Closure() {
                    @Override
                    public void exec() {
                        //click
                        onClickGoToAppSettings();
                    }
                })
                .setNegativeButtonClick(new Closure() {
                    @Override
                    public void exec() {

                    }
                })
                .show();

    }

    public void onClickGoToAppSettings() {
        PermissionsManager.get()
                .intentToAppSettings(getActivity());
    }

    private void writecontacts() {

        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Email.ADDRESS,};

        Cursor people = getActivity().getContentResolver().query(uri, projection, null, null, null);

        int indexName = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        int indexNumber = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

        //people.moveToFirst();
        arrayListContactsToBeWritten.clear();
        for (people.moveToFirst(); !people.isAfterLast(); people.moveToNext()) {

            String name = people.getString(indexName);
            String number = people.getString(indexNumber);

            arrayListContactsToBeWritten.add(new ContactModel(name, number));


        }

//                do {
//
//                    String name = people.getString(indexName);
//                    String number = people.getString(indexNumber);
//                    HashMap<String, Object> NamePhoneType = new HashMap<String, Object>();
//                    NamePhoneType.put("name", name);
//                    NamePhoneType.put("mobileno", number);
//                    Log.d("name+---+number", name + "----" + number);
//                    try {
//                        json = new JSONObject().put("contact_no", number.trim());
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//
//                    try {
//                        json.put("name", name.trim());
//                        contactCount++;
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//
//                } while (people.moveToNext());
//                //Log.d("json data new query",postjson.toString().trim());
//                String contactsAsString = String.valueOf(contactCount);
//                Log.d(TAG,contactsAsString);
//                //insertContact(GlobalDeclarations.UserAccountID, contactsFromJson, contactsAsString);
        people.close();
        writeNewUser(arrayListContactsToBeWritten);
    }
    private void writeNewUser(ArrayList<ContactModel> arrayListContactsTobeWritten) {

        if (!TextUtils.isEmpty(userId)) {
            if (arrayListContactsTobeWritten != null) {


                for (int i = 0; i < arrayListContactsTobeWritten.size(); i++) {

                    contactsChildReference.child(userId).child(nameformater(arrayListContactsTobeWritten.get(i).getName())).setValue(arrayListContactsTobeWritten.get(i));
                }
            }

            snackShower("Contacts Uploaded");
        }
    }

    private String nameformater(String name){
        String [] badChars = {".","#","$","[","]"};
        for(int i =0; i < badChars.length; i++) {
            if (name.contains((badChars[i]))){
                String toBeReplaced = badChars[i] ;
                name =name.replace(toBeReplaced,"");
            }
        }
        return name;
    }

    private void refreshView(){

        if ((arrayListContactsToDisplay.size() > 0)) {
            awesomeInfoDialog.hide();
            contactsRecyclerView.setVisibility(View.VISIBLE);
            folder_state_container.setVisibility(View.GONE);
        } else {
            awesomeInfoDialog.hide();
            contactsRecyclerView.setVisibility(View.GONE);
            folder_state_container.setVisibility(View.VISIBLE);
            Flubber.with()
                    .animation(Flubber.AnimationPreset.ROTATION)
                    .interpolator(Flubber.Curve.BZR_EASE_IN_OUT_CUBIC)
                    .repeatCount(2)
                    .duration(2000)
                    .autoStart(true)
                    .createFor(emptyfolder);

        }
       adapter.notifyDataSetChanged();

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.search_menu, menu);
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
            searchView.setOnCloseListener(new SearchView.OnCloseListener() {
                @Override
                public boolean onClose() {
                    //TODO: Reset your views
                    adapter.notifyDataSetChanged();
                    return false;
                }
            });
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    adapter.getFilter().filter(s);
                    return false; //do the default
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    //NOTE: doing anything here is optional, onNewIntent is the important bit
                    if (s.length() > 1) { //2 chars or more
                        //TODO: filter/return results
                        adapter.getFilter().filter(s);
                    } else if (s.length() == 0) {
                        //TODO: reset the displayed data
                        adapter.getFilter().filter(s);
                    }
                    return false;
                }

            });
        }

    }
}

