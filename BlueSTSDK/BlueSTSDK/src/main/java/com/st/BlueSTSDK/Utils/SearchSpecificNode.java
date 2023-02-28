/*******************************************************************************
 * COPYRIGHT(c) 2015 STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *   1. Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 *   3. Neither the name of STMicroelectronics nor the names of its contributors
 *      may be used to endorse or promote products derived from this software
 *      without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 ******************************************************************************/
package com.st.BlueSTSDK.Utils;

import android.os.AsyncTask;

import com.st.BlueSTSDK.Manager;
import com.st.BlueSTSDK.Node;

import androidx.annotation.NonNull;

import java.util.List;

/**
 * This is a simple class that can be extended for search a node with a particular tag.
 * If you pass more that one tag this task will return the first node tha match one tag
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class SearchSpecificNode extends AsyncTask<String,Void, Node> {

    /**
     * activity used for start/stop the bt scanning
     */
    private NodeScanActivity mActivity;

    /**
     * search time out
     */
    private int mSearchTimeoutMs;


    /**
     * build the task
     * @param scannerActivity activity used for start/stop scanning
     * @param searchTimeMs search timeout
     */
    public SearchSpecificNode(NodeScanActivity scannerActivity, int searchTimeMs){
        mActivity = scannerActivity;
        mSearchTimeoutMs=searchTimeMs;
    }


    /**
     * search the tag on the already discovered node
     * @param tags list of tag that we are searching
     * @return first node that match one tag or null if doesn't find anything
     */
    private Node searchOnAlreadyDiscoveredNodes(String... tags){
        List<Node> nodes = Manager.getSharedInstance().getNodes();
        for(Node n : nodes){
            String nodeTag = n.getTag();
            for(String tag : tags){
                if(nodeTag.equalsIgnoreCase(tag))
                    return n;
            }//for tag
        }//for node
        return null;
    }//searchOnAlreadyDiscoveredNodes

    /**
     * utility instance variable, used since we can't do multiple assignment to a final variable,
     * and we need to declare it final since we have to use inside an anonymous class
     * this variable will contain the found node
     */
    private Node foundNode;
    private Node startNewSearch(final String... tags){
        foundNode=null;
        final Object barrier = new Object();
        final Manager.ManagerListener filterNode = new Manager.ManagerListener() {
            @Override
            public void onDiscoveryChange(@NonNull Manager m, boolean enabled) {
                if(!enabled) //when stop, unlock the barrier
                    synchronized (barrier){
                        barrier.notify();
                    }
            }

            /**
             * check if the new node match the one tag and if do it stop the search
             * @param m    manager that discover the node
             * @param node new node discovered
             */
            @Override
            public void onNodeDiscovered(@NonNull Manager m, Node node) {
                String foundTag = node.getTag();
                for(String temp : tags){
                    if(foundTag.equalsIgnoreCase(temp)) {
                        foundNode = node;
                        mActivity.stopNodeDiscovery();
                    }//if
                }//for
            }//onNodeDiscovered
        };

        Manager.getSharedInstance().addListener(filterNode);

        synchronized (barrier){
            //start the search and wait the end
            mActivity.startNodeDiscovery(mSearchTimeoutMs);
            try {
                barrier.wait();
            } catch (InterruptedException e) {
            }
        }
        //if we are here is because the discovery stops
        Manager.getSharedInstance().removeListener(filterNode);

        return foundNode;
    }

    /**
     * search the tags on the node discovered by the node and start a new search if is not present
     * @param params list of tag to search
     * @return a node that match one tag or null if we doesn't found anything
     */
    @Override
    protected Node doInBackground(final String... params) {

        Node temp = searchOnAlreadyDiscoveredNodes(params);
        if(temp!=null)
            return temp;

        return startNewSearch(params);
    }
}