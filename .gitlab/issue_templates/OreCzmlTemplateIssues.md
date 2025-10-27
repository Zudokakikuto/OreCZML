## 🪐 OreCzml Issue Template

Thank you for contributing to OreCzml!  
Please take a few moments to fill out this issue form carefully — it will help us diagnose and fix the problem faster.

---

### 🧩 The description of the problem :
<!-- 
Describe clearly what the problem is.
What did you expect to happen? What happened instead?
If applicable, include error logs, stack traces, screenshots or a minimal code example.
-->

**Example:**
> When trying to export the CZML file with an Ephemeris-based orbit, the output file is empty.  
> Expected: non-empty file with the correct availability interval.

---

### 💡 The possible solution :
<!-- 
If you have an idea of where the issue might come from or how to fix it, describe it here.
Otherwise, you can leave this section empty.
-->

**Example:**
> It might come from the `InterSatVisu` builder not checking if the `Clock` object is null before serialization.

---

### 🚦 The priority on a scale from 1 to 10 :
<!-- 
1 = very low priority (minor visual bug)
10 = critical (blocks use of OreCzml)
-->

**Priority:** `? / 10`

---

### 🧭 Environment information (optional but recommended)
- **OreCzml version:** (e.g., 1.0.0)
- **Orekit version:** (e.g., 12.1)
- **Java version:** (e.g., 17)
- **Operating system:** (e.g., Windows 11 / Ubuntu 22.04)
- **Execution context:** (e.g., CLI / JUnit test / integration into CesiumJS)

---

### 📎 Additional context
<!-- Any other information or links that could be useful -->
