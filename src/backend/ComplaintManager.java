package backend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ComplaintManager {

    private List<Complaint> complaints;
    private NotificationManager nm;

    // Constructor مع NotificationManager
    public ComplaintManager(NotificationManager nm) {
        this.nm = nm;
        this.complaints = new ArrayList<>();
    }

    // Constructor بدون باراميتر (اختياري)
    public ComplaintManager() {
        this(null);
    }

    // إضافة شكوى جديدة
    public void addComplaint(Complaint c) {
        complaints.add(c);
        if (nm != null) {
            nm.add("Complaint added: " + c.toString());
        }
    }

    // الحصول على جميع الشكاوى (قراءة فقط)
    public List<Complaint> getComplaints() {
        return Collections.unmodifiableList(complaints);
    }

    // البحث عن شكوى بواسطة ID
    public Complaint findComplaintById(String id) {
        for (Complaint c : complaints) {
            if (c.getId().equals(id)) return c;
        }
        return null;
    }

    // حذف شكوى
    public boolean removeComplaint(String id) {
        Complaint c = findComplaintById(id);
        if (c != null) {
            complaints.remove(c);
            if (nm != null) nm.add("Complaint removed: " + c.toString());
            return true;
        }
        return false;
    }
}
