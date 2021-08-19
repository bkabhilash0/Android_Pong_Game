package com.infowithvijay.pong2dgame;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.SurfaceHolder;
import android.view.View;


public class GameThread extends Thread {



    public static final int STATE_READY = 0;
    public static final int STATE_PAUSED = 1;
    public static final int STATE_RUNNING = 2;
    public static final int STATE_WIN = 3;
    public static final int STATE_LOSE = 4;


    private boolean mSensorsOn;

    private final Context mCtx;
    private final SurfaceHolder mSurfaceHolder;
    private final PongTable mPongTable;
    private final Handler mGameStatusHandler;
    private final Handler mScoreHandler;

    private boolean mRun = false;
    private int mGameState;
    private Object mRunLock;


    private static final int PHYS_FPS = 60;

    public GameThread(Context mCtx, SurfaceHolder mSurfaceHolder, PongTable mPongTable, Handler mGameStatusHandler, Handler mScoreHandler) {
        this.mCtx = mCtx;
        this.mSurfaceHolder = mSurfaceHolder;
        this.mPongTable = mPongTable;
        this.mGameStatusHandler = mGameStatusHandler;
        this.mScoreHandler = mScoreHandler;
        mRunLock = new Object();
    }


    @Override
    public void run() {

       long mNextGameTick = SystemClock.uptimeMillis();
       int skipTicks = 1000/ PHYS_FPS;


       while (mRun){
           Canvas c = null;
           try {

               c = mSurfaceHolder.lockCanvas(null);
               if (c!= null){
                   synchronized (mSurfaceHolder){
                       if (mGameState == STATE_RUNNING){
                           mPongTable.update(c);
                       }
                       synchronized (mRunLock){
                           if (mRun){
                               mPongTable.draw(c);
                           }
                       }
                   }
               }

           }catch (Exception e ){
               e.printStackTrace();
           }finally {
               if (c!=null){
                   mSurfaceHolder.unlockCanvasAndPost(c);
               }
           }


           mNextGameTick += skipTicks;
           long sleepTime = mNextGameTick - SystemClock.uptimeMillis();
           if (sleepTime > 0){
               try {
                   Thread.sleep(sleepTime);
               }catch (InterruptedException e){
                   e.printStackTrace();
               }
           }


       }


    }


    public void setState(int state){

        synchronized (mSurfaceHolder){
            mGameState = state;
            Resources res = mCtx.getResources();
            switch (mGameState){

                case STATE_READY:
                   setUpNewRound();
                break;

                case STATE_RUNNING:
                   hideStatusText();
                break;

                case STATE_WIN:
                    setStatusText(res.getString(R.string.mode_win));
                    mPongTable.getPlayer().score++;
                    setUpNewRound();
                    break;

                case STATE_LOSE:
                    setStatusText(res.getString(R.string.mode_loss));
                    mPongTable.getmOpponent().score++;
                    setUpNewRound();
                    break;

                case STATE_PAUSED:
                    setStatusText(res.getString(R.string.mode_paused));
                    break;
            }
        }
    }


    public void setUpNewRound(){
        synchronized (mSurfaceHolder){
            mPongTable.setupTable();
        }
    }


    public void setRunning(boolean running){
        synchronized (mRunLock){
            mRun = running;
        }
    }


   public boolean SensorsOn(){
        return mSensorsOn;
   }


   public boolean isBetweenRounds(){
        return mGameState != STATE_RUNNING;
   }



   private void setStatusText(String text){

        Message msg = mGameStatusHandler.obtainMessage();
        Bundle b = new Bundle();
        b.putString("text",text);
        b.putInt("visibility", View.VISIBLE);
        msg.setData(b);
        mGameStatusHandler.sendMessage(msg);

    }

   private void hideStatusText(){

       Message msg = mGameStatusHandler.obtainMessage();
       Bundle b = new Bundle();
       b.putInt("visibility",View.INVISIBLE);
       msg.setData(b);
       mGameStatusHandler.sendMessage(msg);

   }


   public void setScoreText(String playerScore,String opponentScore){
       Message msg = mScoreHandler.obtainMessage();
       Bundle b = new Bundle();
       b.putString("player",playerScore);
       b.putString("opponent",opponentScore);
       msg.setData(b);
       mScoreHandler.sendMessage(msg);


   }


}
