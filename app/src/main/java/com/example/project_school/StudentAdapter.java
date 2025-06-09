package com.example.project_school;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import java.util.List;

class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    public interface OnStudentClickListener {
        void onCallClick(String phoneNumber);
        void onEmailClick(String email);
        void onStudentClick(Student student);
    }

    private Context context;
    private List<Student> students;
    private OnStudentClickListener listener;

    public StudentAdapter(Context context, List<Student> students, OnStudentClickListener listener) {
        this.context = context;
        this.students = students;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_student, parent, false);
        return new StudentViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        Student student = students.get(position);

        // Basic info
        holder.textName.setText(student.getName());
        holder.textClass.setText(student.getFullClass());
        holder.textEmail.setText(student.getEmail());
        holder.textPhone.setText(student.getPhone());
        holder.textBloodGroup.setText(student.getBloodGroup());
        holder.textAdmissionDate.setText("Admitted: " + student.getAdmissionDate());

        // Avatar with initials
        holder.textAvatar.setText(student.getInitials());

        // Status chip
        holder.chipStatus.setText(student.getStatus().toUpperCase());
        if (student.isActive()) {
            holder.chipStatus.setChipBackgroundColorResource(R.color.status_active);
            holder.chipStatus.setTextColor(ContextCompat.getColor(context, android.R.color.white));
        } else {
            holder.chipStatus.setChipBackgroundColorResource(R.color.status_inactive);
            holder.chipStatus.setTextColor(ContextCompat.getColor(context, android.R.color.white));
        }

        // Click listeners
        holder.iconCall.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCallClick(student.getPhone());
            }
        });

        holder.iconEmail.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEmailClick(student.getEmail());
            }
        });

        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onStudentClick(student);
            }
        });

        // Card elevation animation
        holder.cardView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    holder.cardView.setCardElevation(12f);
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    holder.cardView.setCardElevation(4f);
                    break;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    static class StudentViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView textAvatar, textName, textClass, textEmail, textPhone, textBloodGroup, textAdmissionDate;
        Chip chipStatus;
        ImageView iconCall, iconEmail;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            textAvatar = itemView.findViewById(R.id.textAvatar);
            textName = itemView.findViewById(R.id.textStudentName);
            textClass = itemView.findViewById(R.id.textStudentClass);
            textEmail = itemView.findViewById(R.id.textStudentEmail);
            textPhone = itemView.findViewById(R.id.textStudentPhone);
            textBloodGroup = itemView.findViewById(R.id.textStudentBloodGroup);
            textAdmissionDate = itemView.findViewById(R.id.textAdmissionDate);
            chipStatus = itemView.findViewById(R.id.chipStatus);
            iconCall = itemView.findViewById(R.id.iconCall);
            iconEmail = itemView.findViewById(R.id.iconEmail);
        }
    }
}