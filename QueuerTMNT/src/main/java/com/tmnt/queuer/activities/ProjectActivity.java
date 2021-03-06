package com.tmnt.queuer.activities;

        import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tmnt.queuer.R;
import com.tmnt.queuer.adapters.ProjectAdapter;
import com.tmnt.queuer.databases.TaskDataSource;
import com.tmnt.queuer.models.Task;
import com.tmnt.queuer.views.EnhancedListView;

import java.util.ArrayList;

//created by Bill (not really haha)

    public class ProjectActivity extends ActionBarActivity {
        private int project_id;
        private ArrayList<Task> tasks = new ArrayList<Task>();
        private ProjectAdapter adapter;
        private TextView no_tasks;
        private int projectColor;
        private int maxNumber = 1;
        private String project_name;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            project_id = getIntent().getIntExtra("project_id",-1);
            project_name = getIntent().getStringExtra("project_name");
            projectColor = getIntent().getIntExtra("project_color", Color.BLUE);

            TaskDataSource taskDataSource = new TaskDataSource(this);
            taskDataSource.open();
            ArrayList<Task> allTasks = taskDataSource.getAllTasks();
            for(Task currentTask : allTasks){
                if (currentTask.getProject_id() == project_id){
                    tasks.add(currentTask);
                }
            }


            taskDataSource.close();

            setContentView(R.layout.activity_project);
            this.getWindow().getDecorView().setBackgroundColor(projectColor);

            ActionBar actionBar = getSupportActionBar();
            actionBar.setTitle(project_name);

            no_tasks = (TextView)findViewById(R.id.lv_no_tasks);
            no_tasks.setVisibility(View.INVISIBLE);


            for (Task tempTask: tasks ) {
                if (tempTask.getId() > maxNumber) {
                    maxNumber = tempTask.getId();
                }
            }


            if (tasks.isEmpty()) {
                no_tasks.setVisibility(View.VISIBLE);
            }

            EnhancedListView listView = (EnhancedListView)findViewById(R.id.lv_tasks);
            adapter = new ProjectAdapter(this, tasks);
            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    final Task current_task = adapter.getItem(position);

                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ProjectActivity.this);
                        // set title
                        alertDialogBuilder.setTitle("Edit Task");



                        final View layout = getLayoutInflater().inflate(R.layout.edit_task, null);
                        final EditText taskTitle = (EditText)layout.findViewById(R.id.task_Name);
                        taskTitle.setText(current_task.getName());
                        final EditText taskOrder = (EditText)layout.findViewById(R.id.task_Position);



                    // set dialog message
                        alertDialogBuilder
                                //.setMessage(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)))
                                .setCancelable(true)
                                .setView(layout)


                                .setPositiveButton("Ok",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                current_task.setName(taskTitle.getText().toString());
                                                current_task.updateTask(ProjectActivity.this);
                                                adapter.notifyDataSetChanged();

                                            }
                                        })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                    }
                                });
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }

            });

            listView.setDismissCallback(new EnhancedListView.OnDismissCallback() {
                @Override
                public EnhancedListView.Undoable onDismiss(EnhancedListView listView, final int position) {
                    final Task task = adapter.getItem(position);
                    adapter.remove(position);
                    return new EnhancedListView.Undoable() {
                        @Override
                        public void undo() {
                            adapter.insert(task, position);
                        }
                    };
                }
            });

            listView.enableSwipeToDismiss();
            listView.enableRearranging();
        }



        @Override
        public boolean onCreateOptionsMenu(Menu menu) {

            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.project, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();

            if (id == R.id.task_logout){
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(ProjectActivity.this, R.string.logged_out, duration);
                toast.show();
                Intent go_to_login = new Intent(ProjectActivity.this, LoginActivity.class);
                startActivity(go_to_login);
            }


            if (id == R.id.action_add_task) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                // set title
                alertDialogBuilder.setTitle("New Task");

                View layout = getLayoutInflater().inflate(R.layout.new_task, null);

                final EditText taskTitle = (EditText)layout.findViewById(R.id.task_Name);
                final EditText taskOrder = (EditText)layout.findViewById(R.id.task_Position);

                // set dialog message
                alertDialogBuilder
                        //.setMessage(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)))
                        .setCancelable(true)
                        .setView(layout)
                        .setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Task task = new Task(ProjectActivity.this, project_id, maxNumber + 1, taskTitle.getText().toString());
                                        int pos = Integer.parseInt(taskOrder.getText().toString());
                                        maxNumber++;
                                        task.setOrder(pos);
                                        task.updateTask(ProjectActivity.this);
                                        adapter.insert(task, pos);
                                        adapter.notifyDataSetChanged();

                                    }
                                })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {}
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        public void hide_empty_tasks(){
            no_tasks.setVisibility(View.INVISIBLE);
        }

    }

