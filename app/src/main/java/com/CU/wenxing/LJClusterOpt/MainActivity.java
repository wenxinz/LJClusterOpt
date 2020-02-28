package com.CU.wenxing.LJClusterOpt;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.CU.wenxing.LJClusterOpt.chem.MyVector;
import com.CU.wenxing.LJClusterOpt.chem.MySystem;

import static java.lang.Math.log10;
import static java.lang.Math.pow;

public class MainActivity extends AppCompatActivity {
    public static String TAG = "MAIN ACTIVITY";

    private GLSurfaceView mGLView;
    private float mPreviousX;
    private float mPreviousY;
    private float TOUCH_SCALE_FACTOR = 180.0f/320;
    private MyGLRenderer mRenderer;
    private double[] ljclusterGlobalMinima = {
            -1.0000, -3.0000, -6.0000, -9.103852, -12.712062,
            -16.505384, -19.821489, -24.113360, -28.422532, -32.765970,
            -37.967600, -44.326801, -47.845157, -52.322627, -56.815742,
            -61.317995, -66.530949, -72.659782, -77.177043, -81.684571,
            -86.809782, -92.844472, -97.348815, -102.372663, -108.315616,
            -112.873584, -117.822402
    };
    private ProgressBar progressBar;
    private double minEnergyDiffPerParticle;
    private double progressbarmax;
    private int k = 100;
    private double factor = 2.0;

    private SimulationActivity mSimulationActivity;
    private MySystem mSimulationSystem;
    private Handler uihandler;

    TextView typeView;
    TextView energyView;
    TextView numstepsView;

    private MyVector[] positions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSimulationSystem = new MySystem(Parameters.NUM_OF_PARTICLES,
                Parameters.BOUNDARY_X,Parameters.BOUNDARY_Y,Parameters.BOUNDARY_Z);

        uihandler = new Handler(){
            @Override
            public void handleMessage(final Message msg){
                switch (msg.what){
                    case SimulationActivity.UPDATE_GLVIEW:
                        positions = (MyVector[])msg.obj;
                        mGLView.queueEvent(new Runnable() {
                            @Override
                            public void run() {
                                mRenderer.updateView(positions);
                            }
                        });
                        mGLView.requestRender(); break;
                    case SimulationActivity.UPDATE_TOOLBAR:
                        invalidateOptionsMenu(); break;
                    case SimulationActivity.UPDATE_DISPLAY:
                        energyView.setText(String.format("%.4f",((SimulationActivity.Data)msg.obj).simulsystem.getPotentialE()));
                        numstepsView.setText(String.valueOf(((SimulationActivity.Data)msg.obj).simul.getNumOfSteps()));
                        typeView.setText(Parameters.TYPE);
                        double energyDiffPerParticle = log10((((SimulationActivity.Data)msg.obj).simulsystem.getPotentialE()-Parameters.GLOBAL_MINIMA)/((SimulationActivity.Data)msg.obj).simulsystem.getNumOfParticles()) + 4;
                        if(energyDiffPerParticle < minEnergyDiffPerParticle){
                            minEnergyDiffPerParticle = energyDiffPerParticle;
                        }
                        progressBar.setProgress((int)(pow(minEnergyDiffPerParticle/progressbarmax,factor)*k*minEnergyDiffPerParticle));
                        progressBar.setSecondaryProgress((int)(pow(energyDiffPerParticle/progressbarmax,factor)*k*energyDiffPerParticle));
                        break;
                    case SimulationActivity.UPDATE_MINENERGYDIF:
                        minEnergyDiffPerParticle = progressbarmax; break;
                }
            }
        };

        mSimulationActivity = new SimulationActivity(uihandler,mSimulationSystem);
        mSimulationActivity.start();
        mSimulationActivity.getLooper();

        mGLView = new GLSurfaceView(this);
        mRenderer = new MyGLRenderer(this, mSimulationSystem.getParticlePositions());
        mGLView.setEGLContextClientVersion(2);
        mGLView.setPreserveEGLContextOnPause(true);
        mGLView.setRenderer(mRenderer);
        mGLView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mGLView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent != null){
                    float x = motionEvent.getX();
                    float y = motionEvent.getY();
                    switch (motionEvent.getAction()){
                        case MotionEvent.ACTION_MOVE:
                            final float dx = (x - mPreviousX)* TOUCH_SCALE_FACTOR;
                            final float dy = (y - mPreviousY)* TOUCH_SCALE_FACTOR;
                            mGLView.queueEvent(new Runnable() {
                                @Override
                                public void run() {
                                    mRenderer.handleOnTouchRotation(dx,dy);
                                    mGLView.requestRender();
                                }
                            });
                    }
                    mPreviousX = x;
                    mPreviousY = y;
                    return true;
                }else{
                    return false;
                }
            }
        });
        setContentView(mGLView);

        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        RelativeLayout layout1 = new RelativeLayout(this);
        RelativeLayout.LayoutParams layoutParamsDisplay = new RelativeLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        View relativeLayoutView = inflater.inflate(R.layout.display, layout1, false);
        layout1.addView(relativeLayoutView);
        addContentView(layout1, layoutParamsDisplay);

        // progressbar
        progressBar = (ProgressBar)findViewById(R.id.vertical_progressbar);

        // seekbar
        SeekBar seekBar = (SeekBar)findViewById(R.id.clustersize_selector);
        final TextView clustersize = (TextView)findViewById(R.id.clustersize);
        clustersize.setText(String.valueOf(Parameters.NUM_OF_PARTICLES));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                Parameters.NUM_OF_PARTICLES = i+2;
                clustersize.setText(String.valueOf(Parameters.NUM_OF_PARTICLES));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setClusterSizeDependentProperty();
                Parameters.RESTART = true;
            }
        });

        // toolbar
        Toolbar toolBar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);

        setClusterSizeDependentProperty();

        typeView = (TextView)findViewById(R.id.type);
        energyView = (TextView)findViewById(R.id.energy_view);
        numstepsView = (TextView)findViewById(R.id.numsteps_view);

        mSimulationActivity.runSimulation();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        mSimulationActivity.quit();
    }

    private void setClusterSizeDependentProperty(){
        Parameters.NUM_OF_MC_STEPS = (int)(71.23279*pow(1.16402,Parameters.NUM_OF_PARTICLES));
        Parameters.GLOBAL_MINIMA = ljclusterGlobalMinima[Parameters.NUM_OF_PARTICLES-2];
        progressbarmax = log10(-Parameters.GLOBAL_MINIMA/Parameters.NUM_OF_PARTICLES) + 4;
        progressBar.setMax((int)(progressbarmax*k));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem actionItem = menu.findItem(R.id.action);
        if(Parameters.RUN){
            actionItem.setIcon(R.drawable.ic_action_pause);
        }else{
            actionItem.setIcon(R.drawable.ic_action_play);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.action:
                Parameters.RUN = !Parameters.RUN;
                invalidateOptionsMenu();
                return true;
            case R.id.reinit:
                Parameters.RESTART = true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
