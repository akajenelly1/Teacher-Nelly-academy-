# Teacher Nelly's Young Learners Academy Android App

This project wraps the **existing live public academy** in an Android WebView. It does not modify the Netlify website or the private admin portal.

Public academy loaded by the app:
`https://teachernellyacademi.netlify.app/`

Package/applicationId: `com.teachernelly.younglearners`
Target/compile SDK: 34
Minimum SDK: 24
Java: 17
Gradle: 8.7

The live site's existing `/ .netlify/functions/academy-api` calls remain same-origin because the app loads the live Netlify URL. This preserves the working registration, inquiries, payments and VIP-code flow.

## Build on GitHub from a phone
1. Create/open repository `Teacher-Nelly-young-learners-academy-`.
2. Upload the files in this project to the repository root.
3. Commit to `main`.
4. Open **Actions** → **Build Teacher Nelly Academy APK**.
5. Open the completed run → **Artifacts** → `Teacher-Nelly-Academy-debug`.
6. Download the APK and install it on the Android phone.

The APK is intentionally a separate Android project. Do not replace the website's `index.html` or `admin.html` with these Android files.
