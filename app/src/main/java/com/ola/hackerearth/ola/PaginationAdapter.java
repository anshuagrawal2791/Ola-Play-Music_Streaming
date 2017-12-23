package com.ola.hackerearth.ola;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;

import org.json.JSONArray;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Created by anshu on 19/12/17.
 */
public class PaginationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable  {
    private ArrayList<Song> songs;
    private ArrayList<Song> listFiltered;
    private  Context context;
    private boolean isLoadingAdded = false;
    private static final int LOADING =1;
    private static final int ITEM =2;
    VolleySingleton volleySingleton;
    ImageLoader imageLoader;

    public PaginationAdapter(Context context) {
        songs = new ArrayList<>();
        listFiltered = new ArrayList<>();
        volleySingleton = VolleySingleton.getinstance();
        imageLoader = volleySingleton.getimageloader();
        this.context=context;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(context);

        switch (viewType) {
            case ITEM:
                viewHolder = getViewHolder(parent, inflater);
                break;
            case LOADING:
                View v2 = inflater.inflate(R.layout.item_progress, parent, false);
                viewHolder = new LoadingVH(v2);
                break;
        }
        return viewHolder;

    }

    private RecyclerView.ViewHolder getViewHolder(ViewGroup parent, LayoutInflater inflater) {
        RecyclerView.ViewHolder viewHolder;
        View v1 = inflater.inflate(R.layout.item_list, parent, false);
        viewHolder = new SongVH(v1);
        return viewHolder;
    }

    public void addSong(Song song){
        songs.add(song);
//        listFiltered.add(song);
//        notifyItemInserted(listFiltered.size() - 1);
    }

    public void remove(Song song){
        int position = songs.indexOf(song);
        if (position > -1) {
            songs.remove(position);
            notifyItemRemoved(position);
        }
    }
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder,final int position) {

        final Song song = listFiltered.get(position);

        switch (getItemViewType(position)) {
            case ITEM:
                final SongVH songVH = (SongVH) holder;

                songVH.textView.setText(song.getSong());
                final ByteArrayOutputStream bStream = new ByteArrayOutputStream();
                final byte[][] byteArray = new byte[1][1];

                if(!song.getCover_image().matches(""))
                {
                   String imageurl = song.getCover_image();
                    imageLoader.get(imageurl, new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                            songVH.imageView.setImageDrawable(null);
                            Bitmap b = response.getBitmap();
                            songVH.imageView.setImageBitmap(b);
//                            b.compress(Bitmap.CompressFormat.PNG, 100, bStream);
//                            byteArray[0] = bStream.toByteArray();
                        }

                        @Override
                        public void onErrorResponse(com.android.volley.VolleyError error) {
                            songVH.imageView.setImageDrawable(context.getResources().getDrawable(R.mipmap.info));

                        }
                    });
                }
                else
                    songVH.imageView.setImageDrawable(context.getResources().getDrawable(R.mipmap.info));
                StringBuilder sb = new StringBuilder();

                for(int i=0;i<song.getArtists().length;i++){
                    if(i==0){
                        sb.append("Artists: "+song.getArtists()[i]);
                    }
                    else{
                        sb.append(", "+song.getArtists()[i]);
                    }
                }
                songVH.artist.setText(sb.toString());

                View.OnClickListener list = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context,PlayActivity.class);
                        intent.putExtra(Constants.INTENT_TITLE,song.getSong());
                        intent.putExtra(Constants.INTENT_IMAGE_URL, song.getCover_image());
                        intent.putExtra(Constants.INTENT_TRACK_URL,song.getUrl());
                        intent.putExtra(Constants.INTENT_ARTISTS,new JSONArray(Arrays.asList(song.getArtists())).toString());
                        context.startActivity(intent);
                    }
                };
                songVH.imageView.setOnClickListener(list);
                songVH.textView.setOnClickListener(list);
                songVH.play.setOnClickListener(list);
                break;
            case LOADING:
//                Do nothing
                break;
        }

    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }
    public void addLoadingFooter() {
        isLoadingAdded = true;
        addSong(new Song());
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;

        int position = songs.size() - 1;
        Song item = getItem(position);

        if (item != null) {
//            songs.remove(position);
            for(int i=0;i<songs.size();i++){
                if(songs.get(i).getSong()==null){
                    songs.remove(i);
                    i--;
                }
            }
//            notifyItemRemoved(position);
        }
    }

    public Song getItem(int position) {
        return listFiltered.get(position);
    }


    @Override
    public int getItemCount() {
        return (listFiltered==null)?0:listFiltered.size();
    }

    @Override
    public int getItemViewType(int position) {
        return (position == listFiltered.size() - 1 && isLoadingAdded) ? LOADING : ITEM;
    }

    public void clearAll() {
        isLoadingAdded = false;
        listFiltered.clear();
    }

    public void addAll(ArrayList<Song> songs) {
        for (Song s : songs) {
            addSong(s);
        }
//        listFiltered.clear();
        listFiltered.addAll(songs);
        Log.e("lists",songs.toString()+"\n"+listFiltered.toString());
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {

                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    listFiltered.clear();
                    listFiltered.addAll(songs);
                } else {
                    ArrayList<Song> filteredList = new ArrayList<>();
                    for (Song row : songs) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getSong().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }

                    listFiltered = filteredList;
                }
                Log.e("filter called",listFiltered.toString());
                FilterResults filterResults = new FilterResults();
                filterResults.values = listFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                listFiltered = (ArrayList<Song>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }


    protected class SongVH extends RecyclerView.ViewHolder {
        private TextView textView;
        private CircleImageView imageView;
        private TextView artist;
        public ImageView play;

        public SongVH(View itemView) {
            super(itemView);

            textView = (TextView) itemView.findViewById(R.id.title);
            imageView=(CircleImageView) itemView.findViewById(R.id.imageview);
            artist = (TextView) itemView.findViewById(R.id.artists);
            play = (ImageView)itemView.findViewById(R.id.play);
        }
    }


    protected class LoadingVH extends RecyclerView.ViewHolder {

        public LoadingVH(View itemView) {
            super(itemView);
        }
    }
}
