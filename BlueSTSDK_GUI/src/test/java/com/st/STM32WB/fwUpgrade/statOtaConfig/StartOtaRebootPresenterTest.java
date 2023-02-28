package com.st.STM32WB.fwUpgrade.statOtaConfig;

import com.st.STM32WB.fwUpgrade.feature.RebootOTAModeFeature;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StartOtaRebootPresenterTest {

    private static final short SECTOR_TO_DELETE = 0XFF;
    private static final short NUM_SECTOR_TO_DELETE = 0XFE;

    @Mock
    private StartOtaConfigContract.View mView;
    @Mock
    private RebootOTAModeFeature mFeature;

    @Captor
    private ArgumentCaptor<Runnable> onRebootSend;

    private StartOtaRebootPresenter mPresenter;

    @Before
    public void setUp(){

        when(mView.getSectorToDelete()).thenReturn(SECTOR_TO_DELETE);
        when(mView.getNSectorToDelete()).thenReturn(NUM_SECTOR_TO_DELETE);

        mPresenter = new StartOtaRebootPresenter(mView,mFeature);
    }

    @Test
    public void whenRebootIsPressedTheCommand(){
        mPresenter.onRebootPressed();
        verify(mView).getNSectorToDelete();
        verify(mView).getSectorToDelete();

        verify(mFeature).rebootToFlash(eq(SECTOR_TO_DELETE),eq(NUM_SECTOR_TO_DELETE),any());
    }

    @Test
    public void whenRebootIsPressedTheStartUploadIsCalled(){
        mPresenter.onRebootPressed();

        verify(mFeature).rebootToFlash(eq(SECTOR_TO_DELETE),eq(NUM_SECTOR_TO_DELETE),onRebootSend.capture());

        onRebootSend.getValue().run();

        verify(mView).performFileUpload();
    }

}