package in.gitbox.cryptonian;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sagar on 23-02-2018.
 */

public abstract class FirebaseRecyclerAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH > implements Filterable {

    private static final String LOG_TAG = FirebaseRecyclerAdapter.class.getSimpleName();
    private Query mRef;
    private Class<T> mModelClass;
    private int mLayout;
    LayoutInflater mInflater;
    protected Class<VH> mViewHolderClass;
    private List<T> mModels;
    private List<T> mFilteredModels;
    private List<String> mKeys = new ArrayList<>();
    private Map<String, T> mModelKeys;
    private Map<String, T> mFilteredKeys;
    private ChildEventListener mListener;
    private FirebaseRecyclerAdapter.ValueFilter valueFilter;


    /**
     * @param mRef        The Firebase location to watch for data changes. Can also be a slice of a location, using some
     *                    combination of <code>limit()</code>, <code>startAt()</code>, and <code>endAt()</code>,
     * @param mModelClass Firebase will marshall the data at a location into an instance of a class that you provide
     * @param mLayout     This is the mLayout used to represent a single list item. You will be responsible for populating an
     *                    instance of the corresponding view with the data from an instance of mModelClass.
     * @param activity    The activity containing the ListView
     * @param viewHolderClass This is the PostsViewHolder Class which will be used to populate data.
     */
    public FirebaseRecyclerAdapter(Query mRef, Class<T> mModelClass, int mLayout, Activity activity, Class<VH> viewHolderClass) {
        this.mRef = mRef;
        this.mModelClass = mModelClass;
        this.mLayout = mLayout;
        this.mViewHolderClass = viewHolderClass;
        mInflater = activity.getLayoutInflater();
        mModels = new ArrayList<>();
        mModelKeys = new HashMap<>();
        // Look for all child events. We will then map them to our own internal ArrayList, which backs ListView
        mListener = this.mRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                T model = dataSnapshot.getValue(FirebaseRecyclerAdapter.this.mModelClass);
                mModelKeys.put(dataSnapshot.getKey(), model);

                // Insert into the correct location, based on previousChildName
                if (previousChildName == null) {
                    mModels.add(0, model);
                } else {
                    T previousModel = mModelKeys.get(previousChildName);
                    int previousIndex = mModels.indexOf(previousModel);
                    int nextIndex = previousIndex + 1;
                    if (nextIndex == mModels.size()) {
                        mModels.add(model);
                        mKeys.add(dataSnapshot.getKey());
                    } else {
                        mModels.add(nextIndex, model);
                        mKeys.add(dataSnapshot.getKey());
                    }
                }

                notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(LOG_TAG, "onChildChanged");
                // One of the mModels changed. Replace it in our list and name mapping
                String modelName = dataSnapshot.getKey();
                T oldModel = mModelKeys.get(modelName);
                T newModel = dataSnapshot.getValue(FirebaseRecyclerAdapter.this.mModelClass);
                int index = mModels.indexOf(oldModel);

                mModels.set(index, newModel);
                mModelKeys.put(modelName, newModel);

                notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(LOG_TAG, "onChildRemoved");
                // A model was removed from the list. Remove it from our list and the name mapping
                String modelName = dataSnapshot.getKey();
                T oldModel = mModelKeys.get(modelName);
                mModels.remove(oldModel);
                mKeys.remove(dataSnapshot.getKey());
                mModelKeys.remove(modelName);
                notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(LOG_TAG, "onChildMoved");
                // A model changed position in the list. Update our list accordingly
                String modelName = dataSnapshot.getKey();
                T oldModel = mModelKeys.get(modelName);
                T newModel = dataSnapshot.getValue(FirebaseRecyclerAdapter.this.mModelClass);
                int index = mModels.indexOf(oldModel);
                mModels.remove(index);
                if (previousChildName == null) {
                    mModels.add(0, newModel);
                    mKeys.add(dataSnapshot.getKey());
                } else {
                    T previousModel = mModelKeys.get(previousChildName);
                    int previousIndex = mModels.indexOf(previousModel);
                    int nextIndex = previousIndex + 1;
                    if (nextIndex == mModels.size()) {
                        mModels.add(newModel);
                        mKeys.add(dataSnapshot.getKey());
                    } else {
                        mModels.add(nextIndex, newModel);
                        mKeys.add(dataSnapshot.getKey());
                    }
                }
                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("FirebaseListAdapter", "Listen was cancelled, no more updates will occur");
            }
        });
    }

    public void cleanup() {
        // We're being destroyed, let go of our mListener and forget about all of the mModels
        mRef.removeEventListener(mListener);
        mModels.clear();
        mModelKeys.clear();
        mKeys.clear();
    }

    @Override
    public int getItemCount() {
        return mModels.size();
    }

    public T getItem(int position) {
        return mModels.get(position);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        T model = getItem(position);
        populateViewHolder(holder, model, position, mKeys);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemViewType(int position) {
        return mLayout;
    }

    public void remove(String key) {
        T oldModel = mModelKeys.get(key);
        mModels.remove(oldModel);
        mKeys.remove(key);
        mModelKeys.remove(key);
        notifyDataSetChanged();
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        try {
            Constructor<VH> constructor = mViewHolderClass.getConstructor(View.class);
            return constructor.newInstance(view);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Each time the data at the given Firebase location changes, this method will be called for each item that needs
     * to be displayed. The arguments correspond to the mLayout and mModelClass given to the constructor of this class.
     * <p/>
     * Your implementation should populate the view using the data contained in the model.
     *
     * @param viewHolder     The view to populate
     * @param model The object containing the data used to populate the view
     */
    protected abstract void populateViewHolder(VH viewHolder, T model, int position, List<String> mKeys);

    public void addSingle(DataSnapshot snapshot) {
        T model = snapshot.getValue(FirebaseRecyclerAdapter.this.mModelClass);
        mModelKeys.put(snapshot.getKey(), model);
        mModels.add(model);
        mKeys.add(snapshot.getKey());
        notifyDataSetChanged();
    }

    public void update(DataSnapshot snapshot, String key) {
        T oldModel = mModelKeys.get(key);
        T newModel = snapshot.getValue(FirebaseRecyclerAdapter.this.mModelClass);
        int index = mModels.indexOf(oldModel);

        if (index >= 0) {
            mModels.set(index, newModel);
            mModelKeys.put(key, newModel);
            notifyDataSetChanged();
        }
    }

    public boolean exists(String key) {
        return mModelKeys.containsKey(key);
    }

    @Override
    public Filter getFilter() {
        if (valueFilter == null) {
            valueFilter = new FirebaseRecyclerAdapter.ValueFilter();
        }
        return valueFilter;
    }

    protected abstract List<T> filters(List<T> models, CharSequence constraint);

    private class ValueFilter extends Filter {

        //Invoked in a worker thread to filter the data according to the constraint.
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new Filter.FilterResults();
            if (mFilteredModels == null) {
                mFilteredModels = new ArrayList<>(mModels); // saves the original data in mOriginalValues
                mFilteredKeys = new HashMap<>(mModelKeys); // saves the original data in mOriginalValues
            }
            if (constraint != null && constraint.length() > 0) {
                List<T> filtered = filters(mFilteredModels, constraint);

                results.count = filtered.size();
                results.values = filtered;
                mModelKeys = filterKeys(mModels);
            } else {
                results.count = mFilteredModels.size();
                results.values = mFilteredModels;
                mModelKeys = mFilteredKeys;
            }
            return results;
        }


        //Invoked in the UI thread to publish the filtering results in the user interface.
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            Log.d(LOG_TAG, "filter for " + constraint + ", results nr: " + results.count);
            mModels = (List<T>) results.values;

            notifyDataSetChanged();
        }
    }

    protected abstract Map<String, T> filterKeys(List<T> mModels);

}
