## 🛰️ OreCzml Merge Request Template

Thank you for contributing to OreCzml!  
Please fill out this form carefully to ensure your merge request is clear, traceable, and easy to review.

---

### 🧩 The description of the problem :
<!-- 
Describe the problem or context that led to this merge request.
What was not working or what needed improvement?
-->

**Example:**
> The polyline visibility intervals were not synchronized between spacecrafts during propagation, causing desynchronization in CesiumJS.

---

### 💡 The possible solution :
<!-- 
Explain what changes you made to solve the issue.
Mention any classes, methods, or modules that were modified or added.
-->

**Example:**
> Added synchronization logic in `InterSatVisu.java` to ensure all polyline intervals are updated using the same clock reference.

---

### 🔗 The issue it is from :
<!-- 
If this MR resolves or is related to a GitLab issue, reference it here.
Use the GitLab syntax like: Closes #123 or Related to #456
-->

**Example:**
> Closes #78

---

### 🚦 The priority on a scale from 1 to 10 :
<!-- 
1 = minor refactor or documentation change  
10 = critical fix needed before release
-->

**Priority:** `? / 10`

---

### 🧭 Additional notes
<!-- 
Add any additional context for the reviewers.
For example, potential side effects, dependencies, or future follow-up tasks.
-->
