# 🤑 FinancePal: MVVM Architecture with Firebase 💰  

FinancePal is an Android app designed to simplify personal finance management. 💳 Users can add, view, categorize, and manage financial entries (e.g., `Sent` and `Received`) with advanced features for an intuitive experience. 🔍 The app follows modern Android development practices, implementing MVVM architecture with Firebase for authentication and data storage. 🗂️  

---

## 📋 Table of Contents  

- [🚀 Features](#-features)  
- [📱 Screenshots](#-screenshots)  
- [🛠️ Getting Started](#️-getting-started)  
  - [Installation](#installation)  
- [🤖 Usage](#-usage)  
- [🔧 Technologies Used](#-technologies-used)  
- [🙌 Contributing](#-contributing)  

---

## 🚀 Features  

- **MVVM Architecture**: Ensures a clear separation of concerns with ViewModel, Repository, and LiveData for lifecycle-aware UI updates. 🛠️  
- **Firebase Firestore**: Replaces Room Database for real-time cloud storage and efficient document-based data management. ☁️  
- **Firebase Authentication**: Supports Email/Password login and Google Sign-In for seamless authentication. 🔐  
- **Firebase Storage**: Stores images securely in the cloud. 📸  
- **Glide Image Loading**: Efficient image loading and caching with Glide. ⚡  
- **Add Financial Entries**: Users can add entries with details like name, amount, description, date, category, and image. 📝  
- **Categorization**: Sort entries into categories like "Sent" or "Received." 📂  
- **Swipe to Delete & Undo**: Swipe left to delete an entry with an option to undo the action. 🧹↩️  
- **Scrollable Descriptions**: Long descriptions are fully readable with a dedicated scrollable section. 📜  
- **Persistent Storage**: Data remains securely stored in Firebase, even across multiple devices. 💾  
- **Dynamic Filtering**: Quickly filter entries by category for better data navigation. 🔍  
- **Proper Folder Structure**: Organized codebase with clearly separated components (e.g., `data`, `ui`, `viewmodel`, `repository`). 📁  
- **Responsive Layout**: Supports various screen sizes and orientations seamlessly. 📱  
- **Material Design**: Clean and modern UI following Material Design principles. 🎨  
- **User Profile Page**: Allows users to manage their profile details. 👤  
- **User Icon in Top Bar**: Displays the user's profile picture in the app's top bar. 🏷️  

---

## 📱 Screenshots  
![4](https://github.com/user-attachments/assets/caaca43a-8a9f-4f37-bd82-1ce45ef57b6b)

![5](https://github.com/user-attachments/assets/3ffa1607-4c07-42a6-a85e-4292d5ddf55e)

---

## 🛠️ Getting Started  

### Installation  

1. **Clone the Repository**:  
   ```bash  
   git clone https://github.com/skp3214/FinancePal.git  
   ```  

2. **Open in Android Studio**:  
   - Launch Android Studio.  
   - Select "Open an existing project" and choose the `financepal` folder.  

3. **Configure Firebase**:  
   - Go to [Firebase Console](https://console.firebase.google.com/) and create a project.  
   - Download the `google-services.json` file and place it in the `app` directory.  
   - Enable **Firestore**, **Firebase Authentication**, and **Firebase Storage** in Firebase Console.  

4. **Build the Project**:  
   - Click on `Build > Rebuild Project` to sync dependencies and build the project.  

5. **Run the Project**:  
   - Connect a device or start an emulator.  
   - Click on the `Run` button or select `Run > Run 'app'` in the menu.  

---

## 🤖 Usage  

1. **User Authentication**:  
   - Users can log in using Email/Password or Google Sign-In.  
   - If new, they can register before logging in.  

2. **Add Entry**:  
   - Open the app and click on "Add Entry."  
   - Fill in the entry details, including optional image attachment and category selection.  

3. **Swipe to Delete & Undo**:  
   - Swipe left on an entry to delete it.  
   - Use the Undo button to restore the deleted entry.  

4. **Scroll Long Descriptions**:  
   - Tap an entry with a long description to view the full content in a scrollable section.  

5. **Filter Entries**:  
   - Navigate between categories like "Sent" or "Received" to view relevant entries.  

6. **User Profile Page**:  
   - Access and update user details.  
   - View the profile icon in the top bar.  

---

## 🔧 Technologies Used  

- **Kotlin**: Language used for app development. 🚀  
- **Firebase Firestore**: Cloud NoSQL database for document storage. ☁️  
- **Firebase Authentication**: Secure user login with Email/Password and Google Sign-In. 🔐  
- **Firebase Storage**: Stores images in the cloud. 📸  
- **Glide**: Efficient image loading and caching. ⚡  
- **MVVM Architecture**: Separates UI and logic using ViewModel, Repository, and LiveData. 📊  
- **Material Design Components**: Ensures a visually consistent and modern UI. 🎨  
- **Swipe-to-Delete**: Implemented using ItemTouchHelper for RecyclerView. ↩️  
- **RecyclerView with Custom Adapter**: Displays entries with flexible interaction options. 🔌  
- **Android Jetpack Components**: Includes ViewModel, LiveData, and Navigation Component. 🧰  

---

## 🙌 Contributing  

Contributions are welcome! 🤝 Please fork this repository, make your changes, and submit a pull request.  

### Steps to Contribute:  

1. Fork this repository. 🍴  
2. Create a new branch (`git checkout -b firebase`). 🌱  
3. Make your changes and commit them (`git commit -m 'Add feature'`). 💻  
4. Push to the branch (`git push origin firebase`). 🚀  
5. Open a Pull Request. 🔍  

---

