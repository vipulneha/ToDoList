package com.techvipul.todolist;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddEditTaskActivity extends AppCompatActivity {

    private TextInputEditText etTitle, etDescription, etDeadline;
    private Button btnSave, btnSetDeadline;
    private TaskDbHelper dbHelper;
    private Task task;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_task);

        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        etDeadline = findViewById(R.id.et_deadline);
        btnSave = findViewById(R.id.btn_save);
        btnSetDeadline = findViewById(R.id.btn_set_deadline);

        dbHelper = new TaskDbHelper(this);

        // Check if editing an existing task
        if (getIntent().hasExtra("task")) {
            task = (Task) getIntent().getSerializableExtra("task");
            isEditMode = true;
            etTitle.setText(task.getTitle());
            etDescription.setText(task.getDescription());
            if (task.getDeadline() != null && !task.getDeadline().isEmpty()) {
                etDeadline.setText(task.getDeadline());
            }
        }

        btnSetDeadline.setOnClickListener(v -> showDateTimePicker());

        btnSave.setOnClickListener(v -> saveTask());
    }

    private void showDateTimePicker() {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                            (view1, hourOfDay, minute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                                String deadline = sdf.format(calendar.getTime());
                                etDeadline.setText(deadline);
                                if (!isEditMode) {
                                    setReminder(calendar, etTitle.getText().toString());
                                } else {
                                    setReminder(calendar, task.getTitle());
                                }
                            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
                    timePickerDialog.show();
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void setReminder(Calendar calendar, String taskTitle) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("task_title", taskTitle);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (int) System.currentTimeMillis(),
                intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    private void saveTask() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String deadline = etDeadline.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isEditMode) {
            task.setTitle(title);
            task.setDescription(description);
            task.setDeadline(deadline);
            dbHelper.updateTask(task);
        } else {
            Task newTask = new Task(0, title, description, deadline);
            dbHelper.addTask(newTask);
        }

        setResult(RESULT_OK);
        finish();
    }
}