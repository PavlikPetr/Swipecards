package com.topface.topface.requests;

import com.topface.topface.data.FeedUserListData;
import com.topface.topface.data.Leader;
import com.topface.topface.data.Photo;
import com.topface.topface.requests.handlers.ApiHandler;

/**
 * Тест
 */
public class LeadersRequestTest extends AbstractThreadTest {

    public void testLeadersRequestExec() throws Throwable {
        runAsyncTest(new Runnable() {
            @Override
            public void run() {
                sendLeadersRequest();
            }
        }, "testLeadersRequestExec");
    }

    private void sendLeadersRequest() {
        new LeadersRequest(getInstrumentation().getContext())
                .callback(new ApiHandler() {
                    @Override
                    public void success(ApiResponse response) {
                        FeedUserListData<Leader> leaders = new FeedUserListData<Leader>(response.jsonResult, Leader.class);
                        assertNotNull("Leaders result is null", leaders);
                        assertTrue("Leaders result is empty", leaders.size() > 0);
                        for (Leader item : leaders) {
                            assertNotNull("Leader item is null", item);
                            assertTrue("Leader id is incorrect", item.id > 0);
                            assertNotNull("Leader has't city", item.city);
                            assertTrue("Leader city id is incorrect", item.city.id > 0);
                            assertNotNull("Leader photo is null", item.photo);
                            assertTrue("Leader has't original photo", item.photo.getSuitableLink(Photo.SIZE_ORIGINAL) != null);
                            assertTrue("Leader getSuitableLink error", item.photo.getSuitableLink(Photo.SIZE_128) != null);
                        }
                        stopTest("testLeadersRequestExec");
                    }

                    @Override
                    public void fail(int codeError, ApiResponse response) {
                        assertTrue("Request exec fail: " + codeError, false);
                        stopTest("testLeadersRequestExec");
                    }
                })
                .exec();
    }
}
