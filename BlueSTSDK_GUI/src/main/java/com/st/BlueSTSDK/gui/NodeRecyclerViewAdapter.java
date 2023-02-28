/*
 * Copyright (c) 2017  STMicroelectronics – All rights reserved
 * The STMicroelectronics corporate logo is a trademark of STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions
 *   and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice, this list of
 *   conditions and the following disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 * - Neither the name nor trademarks of STMicroelectronics International N.V. nor any other
 *   STMicroelectronics company nor the names of its contributors may be used to endorse or
 *   promote products derived from this software without specific prior written permission.
 *
 * - All of the icons, pictures, logos and other images that are provided with the source code
 *   in a directory whose title begins with st_images may only be used for internal purposes and
 *   shall not be redistributed to any third party or modified in any way.
 *
 * - Any redistributions in binary form shall not include the capability to display any of the
 *   icons, pictures, logos and other images that are provided with the source code in a directory
 *   whose title begins with st_images.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */
package com.st.BlueSTSDK.gui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.st.BlueSTSDK.Manager;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.NumberConversion;
import com.st.BlueSTSDK.fwDataBase.db.OptionByteEnumType;
import com.st.utility.databases.associatedBoard.AssociatedBoard;
import com.st.utility.databases.associatedBoard.ReadAssociatedBoardDataBase;
import com.st.BlueSTSDK.fwDataBase.db.BoardFirmware;
import com.st.BlueSTSDK.fwDataBase.db.OptionByte;
import com.st.BlueSTSDK.fwDataBase.ReadBoardFirmwareDataBase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Adapter view for a list of discovered nodes
 */
public class NodeRecyclerViewAdapter extends RecyclerView.Adapter<NodeRecyclerViewAdapter.ViewHolder>
        implements Manager.ManagerListener {

    private final List<Node> mValues = new ArrayList<>();
    private Context context;


    private final int mMaxIconCode = 55;
    private int mBlueSTSDK_V2_Icon[] = {
            /* 0  -> Low Battery */
            R.drawable.battery_00,
            /* 1  -> Battery ok */
            R.drawable.battery_60,
            /* 2  -> Battery Full */
            R.drawable.battery_100,
            /* 3  -> Battery Charging */
            R.drawable.battery_80c,
            /* 4  -> Message */
            R.drawable.ic_message_24,
            /* 5  -> Warning/Alarm */
            R.drawable.ic_warning_24,
            /* 6  -> Error */
            R.drawable.ic_error_24,
            /* 7  -> Ready */
            R.drawable.ic_ready_outline_24,
            /* 8  -> Waiting Pairing */
            R.drawable.ic_bluetooth_waiting_24,
            /* 9  -> Paired */
            R.drawable.ic_bluetooth_connected_24,
            /* 10 -> Log On going */
            R.drawable.ic_log_on_going_24,
            /* 11 -> Memory Full */
            R.drawable.ic_disc_full_24,
            /* 12 -> Connected to Cloud */
            R.drawable.ic_cloud_done_24,
            /* 13 -> Connecting to Cloud */
            R.drawable.ic_cloud_upload_24,
            /* 14 -> Cloud not Connected */
            R.drawable.ic_cloud_off_24,
            /* 15 -> GPS found */
            R.drawable.ic_gps_fixed_24,
            /* 16 -> GPS not Found */
            R.drawable.ic_gps_not_fixed_24,
            /* 17 -> GPS Off */
            R.drawable.ic_gps_off_24,
            /* 18 -> Led On */
            R.drawable.ic_flash_on_24,
            /* 19 -> Led Off */
            R.drawable.ic_flash_off_24,
            /* 20 -> Link On */
            R.drawable.ic_link_on_24,
            /* 21 -> Link Off */
            R.drawable.ic_link_off_24,
            /* 22 -> Wi-Fi On */
            R.drawable.ic_wifi_on_24,
            /* 23 -> Wi-Fi Off */
            R.drawable.ic_wifi_off_24,
            /* 24 -> Wi-Fi Tethering */
            R.drawable.ic_wifi_tethering_24,
            /* 25 -> Low Power */
            R.drawable.ic_battery_saver_24dp,
            /* 26 -> Sleeping */
            R.drawable.ic_sleep_hotel_24,
            /* 27 -> High Power */
            R.drawable.ic_battery_charging_full_24,
            /* 28 -> Microphone On */
            R.drawable.ic_mic_on_24,
            /* 29 -> Microphone Off */
            R.drawable.ic_mic_off_24,
            /* 30 -> Play */
            R.drawable.ic_play_arrow_24,
            /* 31 -> Pause */
            R.drawable.ic_pause_24,
            /* 32 -> Stop */
            R.drawable.ic_stop_24,
            /* 33 -> Sync On */
            R.drawable.ic_sync_on_24,
            /* 34 -> Sync Off */
            R.drawable.ic_sync_off_24,
            /* 35 -> Sync Error */
            R.drawable.ic_sync_error_24,
            /* 36 -> Lock */
            R.drawable.ic_lock_24,
            /* 37 -> Not Lock */
            R.drawable.ic_lock_open_24,
            /* 38 -> Star */
            R.drawable.ic_star_24,
            /* 39 -> Very dissatisfied */
            R.drawable.ic_very_dissatisfied_24,
            /* 40 -> Dissatisfied */
            R.drawable.ic_dissatisfied_24,
            /* 41 -> Satisfied */
            R.drawable.ic_satisfied_24,
            /* 42 -> Very satisfied */
            R.drawable.ic_very_satisfied_24,
            /* 43 -> Sick */
            R.drawable.ic_sick_24,
            /* 44 -> Share */
            R.drawable.ic_share_24,
            /* 45 -> Filter 1 */
            R.drawable.ic_baseline_filter_1_24,
            /* 46 -> Filter 2 */
            R.drawable.ic_baseline_filter_2_24,
            /* 47 -> Filter 3 */
            R.drawable.ic_baseline_filter_3_24,
            /* 48 -> Filter 4 */
            R.drawable.ic_baseline_filter_4_24,
            /* 49 -> Filter 5 */
            R.drawable.ic_baseline_filter_5_24,
            /* 50 -> Filter 6 */
            R.drawable.ic_baseline_filter_6_24,
            /* 51 -> Filter 7 */
            R.drawable.ic_baseline_filter_7_24,
            /* 52 -> Filter 8 */
            R.drawable.ic_baseline_filter_8_24,
            /* 53 -> Filter 9 */
            R.drawable.ic_baseline_filter_9_24,
            /* 54 -> Filter 9+ */
            R.drawable.ic_baseline_filter_9_plus_24,
            /* 55 (mMaxIconCode) -> Icon Code not Recognized  */
            R.drawable.ic_help_24
    };

    /**
     * Interface to use when a node is selected by the user
     */
    public interface OnNodeSelectedListener {
        /**
         * function call when a node is selected by the user
         *
         * @param n node selected
         */
        void onNodeSelected(@NonNull Node n);

        void onNodeAdded(Node mItem, ImageView mNodeAddedIcon);
    }

    /**
     * Interface used to filter the node
     */
    public interface FilterNode {
        /**
         * function for filter the node to display
         *
         * @param n node to display
         * @return true if the node must be displayed, false otherwise
         */
        boolean displayNode(@NonNull Node n);
    }

    private OnNodeSelectedListener mListener;
    private FilterNode mFilterNode;
    private ReadBoardFirmwareDataBase fwDataBase;
    private ReadAssociatedBoardDataBase associatedDataBase;

    private boolean mShowNewBoard;

    public NodeRecyclerViewAdapter(List<Node> items, OnNodeSelectedListener listener,
                                   FilterNode filter, ReadBoardFirmwareDataBase firmwareDB,
                                   ReadAssociatedBoardDataBase associatedDB, boolean showNewBoard) {
        mListener = listener;
        mFilterNode = filter;
        fwDataBase = firmwareDB;
        associatedDataBase = associatedDB;
        mShowNewBoard = showNewBoard;
        addAll(items);
    }//NodeRecyclerViewAdapter

    @Nullable
    public Node findNodeByMacAddress(String mac) {
        for (int i=0; i<mValues.size(); i++)
        {
            String nodeAddress = mValues.get(i).getTag();
            if(nodeAddress.equals(mac)){
                return mValues.get(i);
            }
        }
        return null;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.node_list_item, parent, false);

        LinearLayout llPlaceholder = view.findViewById(R.id.placeholder_for_custom_view);
        llPlaceholder.addView(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_ble_item, parent, false));
        context = parent.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Node n = mValues.get(position);
        holder.mItem = n;
        holder.mNodeNameLabel.setText(n.getName());
        holder.mNodeTagLabel.setText(n.getTag());

        @DrawableRes int boardImageRes = NodeGui.getRealBoardTypeImage(n.getType());

        Glide.with(context)
                .load(boardImageRes)
                .fitCenter()
                .into(holder.mNodeImage);//imageView
        //Drawable boardImage = ContextCompat.getDrawable(holder.mNodeHasExtension.getContext(),boardImageRes);
        //holder.mNodeImage.setImageDrawable(boardImage);

        //set the connectivity image
        holder.mNodeConnectivity.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.connectivity_ble));
        holder.mNodeConnectivity.setColorFilter(context.getResources().getColor(R.color.colorPrimary));

        AssociatedBoard associatedBoard = associatedDataBase.getBoardDetailsWithMAC(n.getTag());

        byte[] optBytes = NumberConversion.BigEndian.uint32ToBytes(n.getAdvertiseOptionBytes());
        short optBytesUnsigned[] = new short[4];
        optBytesUnsigned[0] = NumberConversion.byteToUInt8(optBytes, 0);
        optBytesUnsigned[1] = NumberConversion.byteToUInt8(optBytes, 1);
        optBytesUnsigned[2] = NumberConversion.byteToUInt8(optBytes, 2);
        optBytesUnsigned[3] = NumberConversion.byteToUInt8(optBytes, 3);

        BoardFirmware fw_details = fwDataBase.getFwDetailsNode((short) (n.getTypeId() & 0xFF),
                optBytesUnsigned[0],
                optBytesUnsigned[1]);

        //Set the Fw Details for the node
        if (n.getAdvertiseInfo().getProtocolVersion() == 2) {
            n.setFwDetails(fw_details);
        }

        //Reset the ViewHolder
        holder.mNodeRunningCodeDemo.setVisibility(View.GONE);
        holder.mNodeRunningIcon1.setVisibility(View.INVISIBLE);
        holder.mNodeRunningIcon2.setVisibility(View.INVISIBLE);
        holder.mNodeRunningIcon3.setVisibility(View.INVISIBLE);
        holder.mNodeRunningLabel1.setVisibility(View.GONE);
        holder.mNodeRunningLabel2.setVisibility(View.GONE);
        holder.mNodeRunningLabel3.setVisibility(View.GONE);
        holder.mNodeCustomFirmware.setVisibility(View.GONE);

        if (associatedBoard != null) {
            holder.mNodeAddedIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_favorite));
            holder.mNodeAddedIcon.setColorFilter(context.getResources().getColor(R.color.colorPrimary));
        } else {
            holder.mNodeAddedIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_not_favorite));
            holder.mNodeAddedIcon.setColorFilter(context.getResources().getColor(R.color.colorPrimary));
        }

        holder.mNodeAddedIcon.setOnClickListener(v -> {
            if (null != mListener) {
                mListener.onNodeAdded(holder.mItem, holder.mNodeAddedIcon);
            }
        });

        if (n.getAdvertiseInfo().getProtocolVersion() == 2) {
            //For SDK protocol v2 we have the option bytes instead of feature Mask

            if (fw_details != null) {
                List<OptionByte> optBytesDesc = fw_details.getOption_bytes();
                //Set the Firmware Name
                String running_firmware = String.format("%s Running Fw %s V%s",
                        fw_details.getBrd_name(),
                        fw_details.getFw_name(),
                        fw_details.getFw_version());
                holder.mNodeRunningCodeDemo.setVisibility(View.VISIBLE);
                holder.mNodeRunningCodeDemo.setText(running_firmware);
                holder.mNodeRunningCodeDemo.setSelected(true);

                //Parse up to 3 remaining option bytes
                //if FirmwareId is == 0x00, we use one more option byte for identifying the FirmwareId
                int offsetForFirstOptByte = optBytesUnsigned[0] == 0x00 ? 1 : 0;
                for (int i = 0; i < optBytesDesc.size(); i++) {
                    OptionByte currentOptByteDesc = optBytesDesc.get(i);
                    if (currentOptByteDesc.getFormat().equals("int")) {
                        //Search if there is a Integer to visualize
                        String display;

                        if (currentOptByteDesc.getEscape_value() != null) {
                            if (currentOptByteDesc.getEscape_value() == optBytesUnsigned[i + 1 + offsetForFirstOptByte]) {
                                display = currentOptByteDesc.getEscape_message();
                            } else {
                                display = String.format(Locale.getDefault(), "%s %d%s",
                                        currentOptByteDesc.getName(),
                                        ((optBytesUnsigned[i + 1 + offsetForFirstOptByte] - currentOptByteDesc.getNegative_offset()) * currentOptByteDesc.getScale_factor()),
                                        currentOptByteDesc.getType());

                            }
                        } else {
                            display = String.format(Locale.getDefault(), "%s %d%s",
                                    currentOptByteDesc.getName(),
                                    ((optBytesUnsigned[i + 1 + offsetForFirstOptByte] - currentOptByteDesc.getNegative_offset()) * currentOptByteDesc.getScale_factor()),
                                    currentOptByteDesc.getType());
                        }
                        if (holder.mNodeRunningLabel1.getVisibility() == View.GONE) {
                            holder.mNodeRunningLabel1.setVisibility(View.VISIBLE);
                            holder.mNodeRunningLabel1.setText(display);
                        } else if (holder.mNodeRunningLabel2.getVisibility() == View.GONE) {
                            holder.mNodeRunningLabel2.setVisibility(View.VISIBLE);
                            holder.mNodeRunningLabel2.setText(display);
                        } else {
                            holder.mNodeRunningLabel3.setVisibility(View.VISIBLE);
                            holder.mNodeRunningLabel3.setText(display);
                        }
                    } else if (currentOptByteDesc.getFormat().equals("enum_string")) {
                        //Search if there is a Enum string to visualize

                        //Search if there is a valid value
                        String found = null;
                        String display;
                        for (OptionByteEnumType element : Objects.requireNonNull(currentOptByteDesc.getString_values())) {
                            if (element.getValue() == optBytesUnsigned[i + 1 + offsetForFirstOptByte]) {
                                found = element.getDisplay_name();
                            }
                        }
                        if (found != null) {
                            display = String.format("%s %s",
                                    currentOptByteDesc.getName(), found);
                        } else {
                            display = String.format("%s", currentOptByteDesc.getName());
                        }


                        if (holder.mNodeRunningLabel1.getVisibility() == View.GONE) {
                            holder.mNodeRunningLabel1.setVisibility(View.VISIBLE);
                            holder.mNodeRunningLabel1.setText(display);
                        } else if (holder.mNodeRunningLabel2.getVisibility() == View.GONE) {
                            holder.mNodeRunningLabel2.setVisibility(View.VISIBLE);
                            holder.mNodeRunningLabel2.setText(display);
                        } else {
                            holder.mNodeRunningLabel3.setVisibility(View.VISIBLE);
                            holder.mNodeRunningLabel3.setText(display);
                        }
                    } else if (currentOptByteDesc.getFormat().equals("enum_icon")) {
                        //Search if there is a Icon to visualize
                        //Search if there is a valid value
                        int found = mMaxIconCode;
                        for (OptionByteEnumType element : Objects.requireNonNull(currentOptByteDesc.getIcon_values())) {
                            if (element.getValue() == optBytesUnsigned[i + 1 + offsetForFirstOptByte]) {
                                found = element.getIcon_code();
                            }
                        }

                        if (holder.mNodeRunningIcon1.getVisibility() == View.INVISIBLE) {
                            if(optBytesUnsigned[i + 1 + offsetForFirstOptByte]!=0xFF) {
                                holder.mNodeRunningIcon1.setVisibility(View.VISIBLE);
                            }
                            setNodeRunningIcon(holder.mNodeRunningIcon1, found);
                            holder.mNodeRunningIcon1.setColorFilter(context.getResources().getColor(R.color.colorPrimary));
                        } else if (holder.mNodeRunningIcon2.getVisibility() == View.INVISIBLE) {
                            if(optBytesUnsigned[i + 1 + offsetForFirstOptByte]!=0xFF) {
                                holder.mNodeRunningIcon2.setVisibility(View.VISIBLE);
                            };
                            setNodeRunningIcon(holder.mNodeRunningIcon2,found);
                            holder.mNodeRunningIcon2.setColorFilter(context.getResources().getColor(R.color.colorPrimary));
                        } else {
                            if(optBytesUnsigned[i + 1 + offsetForFirstOptByte]!=0xFF) {
                                holder.mNodeRunningIcon3.setVisibility(View.VISIBLE);
                            }
                            setNodeRunningIcon(holder.mNodeRunningIcon3,found);
                            holder.mNodeRunningIcon3.setColorFilter(context.getResources().getColor(R.color.colorPrimary));
                        }
                    }
                }

                if (fw_details.getBle_fw_id().equals("0xFF")) {
                    holder.mNodeCustomFirmware.setVisibility(View.VISIBLE);
                }
            }
        }


        //On Press Item
        holder.mView.setOnClickListener(v -> {
            if (null != mListener) {
                // Notify the active callbacks interface (the activity, if the
                // fragment is attached to one) that an item has been selected.
                mListener.onNodeSelected(holder.mItem);
            }
        });

        // On Long Press Item
        if ((n.getAdvertiseInfo().getProtocolVersion() == 2) && (fw_details != null)) {
            holder.mView.setOnLongClickListener(v -> {
                AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                alertDialog.setTitle("BlueST-SDK V2 Node Model");
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                alertDialog.setMessage(gson.toJson(fw_details));

                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Close",
                        (dialog, which) -> dialog.dismiss());
                alertDialog.show();
                return true;
            });
        } else {
            holder.mView.setOnLongClickListener(v -> {
                AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                alertDialog.setTitle("BlueST-SDK V2 Node Model");
                if (n.getAdvertiseInfo().getProtocolVersion() == 2) {
                    //if it's a V2 but we don't have one associated fw_model
                    alertDialog.setMessage("BlueST-SDK V2 unknown model");
                } else {
                    alertDialog.setMessage("This is a BlueST-SDK V1 model.. move to V2!");
                }
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Close",
                        (dialog, which) -> dialog.dismiss());
                alertDialog.show();
                return true;
            });
        }

        if (n.isSleeping()) {
            holder.mNodeIsSleeping.setVisibility(View.VISIBLE);
        } else {
            holder.mNodeIsSleeping.setVisibility(View.INVISIBLE);
        }

        if (n.hasGeneralPurpose()) {
            holder.mNodeHasExtension.setVisibility(View.VISIBLE);
        } else {
            holder.mNodeHasExtension.setVisibility(View.INVISIBLE);
        }
    }

    private void setNodeRunningIcon(ImageView runningIcon, int iconCode) {
        if (iconCode >= mMaxIconCode)
            iconCode = mMaxIconCode;
        runningIcon.setImageResource(mBlueSTSDK_V2_Icon[iconCode]);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public Node getItemPosition(int position) {
        return mValues.get(position);
    }

    public void setBoardToShow(boolean showNewBoard) {
        mShowNewBoard = showNewBoard;
    }

    @Override
    public void onDiscoveryChange(@NonNull Manager m, boolean enabled) {

    }

    public void clear() {
        mValues.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Node> items) {
        for (Node n : items) {
            if (mFilterNode.displayNode(n)) {
                AssociatedBoard associatedBoard = associatedDataBase.getBoardDetailsWithMAC(n.getTag());
                if (mShowNewBoard) {
                    if (associatedBoard == null) {
                        mValues.add(n);
                    }
                } else {
                    if (associatedBoard != null) {
                        mValues.add(n);
                    }
                }
            }//if
        }//for
        notifyDataSetChanged();
    }

    private Handler mUIThread = new Handler(Looper.getMainLooper());

    @Override
    public void onNodeDiscovered(@NonNull Manager m, @NonNull final Node node) {
        if (mFilterNode.displayNode(node)) {
            mUIThread.post(() -> {
                AssociatedBoard associatedBoard = associatedDataBase.getBoardDetailsWithMAC(node.getTag());
                if (mShowNewBoard) {
                    if (associatedBoard == null) {
                        mValues.add(node);
                        notifyItemInserted(mValues.size() - 1);
                    }
                } else {
                    if (associatedBoard != null) {
                        mValues.add(node);
                        notifyItemInserted(mValues.size() - 1);
                    }
                }
            });
        }//if
    }//onNodeDiscovered

    public class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final ImageView mNodeConnectivity;
        final TextView mNodeNameLabel;
        final TextView mNodeTagLabel;
        final ImageView mNodeImage;
        final ImageView mNodeIsSleeping;
        final ImageView mNodeHasExtension;
        final TextView mNodeRunningCodeDemo;
        final ImageView mNodeRunningIcon1;
        final ImageView mNodeRunningIcon2;
        final ImageView mNodeRunningIcon3;
        final TextView mNodeRunningLabel1;
        final TextView mNodeRunningLabel2;
        final TextView mNodeRunningLabel3;
        final ImageView mNodeAddedIcon;
        final CardView mNodeCardView;
        final TextView mNodeCustomFirmware;
        Node mItem;


        ViewHolder(View view) {
            super(view);
            mView = view;
            mNodeConnectivity = view.findViewById(R.id.iv_connectivity);
            mNodeImage = view.findViewById(R.id.nodeBoardIcon);
            mNodeNameLabel = view.findViewById(R.id.nodeName);
            mNodeTagLabel = view.findViewById(R.id.node_id);
            mNodeHasExtension = view.findViewById(R.id.hasExtensionIcon);
            mNodeIsSleeping = view.findViewById(R.id.isSleepingIcon);
            mNodeRunningCodeDemo = view.findViewById(R.id.nodeRunningCodeName);
            mNodeRunningIcon1 = view.findViewById(R.id.nodeRunningIcon1);
            mNodeRunningIcon2 = view.findViewById(R.id.nodeRunningIcon2);
            mNodeRunningIcon3 = view.findViewById(R.id.nodeRunningIcon3);
            mNodeRunningLabel1 = view.findViewById(R.id.nodeRunningLabel1);
            mNodeRunningLabel2 = view.findViewById(R.id.nodeRunningLabel2);
            mNodeRunningLabel3 = view.findViewById(R.id.nodeRunningLabel3);
            mNodeAddedIcon = view.findViewById(R.id.nodeAddedIcon);
            mNodeCardView = view.findViewById(R.id.nodeCardView);
            mNodeCustomFirmware = view.findViewById(R.id.nodeCustomFirmware);
        }
    }
}
