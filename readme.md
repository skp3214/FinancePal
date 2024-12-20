# 🤑 FinancePal: MVVM Architecture 💰  

FinancePal is an Android app designed to simplify personal finance management. 💳 Users can add, view, categorize, and manage financial entries (e.g., `Sent` and `Received`) with advanced features for an intuitive experience. 🔍 The app leverages modern Android development practices, implementing MVVM architecture with Room Database for robust data management. 🗂️  

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
- **Room Database**: Provides efficient and reliable local data storage with entity-based schema. 🗂️  
- **Add Financial Entries**: Add entries with details like name, amount, description, date, category, and optional image. 📝  
- **Categorization**: Sort entries into categories like "Sent" or "Received." 📂  
- **Swipe to Delete & Undo**: Swipe left to delete an entry with an option to undo the action. 🧹↩️  
- **Scrollable Descriptions**: Long descriptions are fully readable with a dedicated scrollable section. 📜  
- **Persistent Storage**: Data remains stored locally even after the app is closed. 💾  
- **Dynamic Filtering**: Quickly filter entries by category for better data navigation. 🔍  
- **Proper Folder Structure**: Organized codebase with clearly separated components (e.g., `data`, `ui`, `viewmodel`, `repository`). 📁  
- **Responsive Layout**: Supports various screen sizes and orientations seamlessly. 📱  
- **Material Design**: Clean and modern UI following Material Design principles. 🎨  

---

## 📱 Screenshots  

![screenshot](https://github.com/user-attachments/assets/fe5d6c6f-5558-427b-bf58-fc49baaea037)

---

## 🛠️ Getting Started  

### Installation  

1. **Clone the Repository**:  
   ```bash  
   git clone -b roomdb https://github.com/skp3214/FinancePal.git  
   ```  

2. **Open in Android Studio**:  
   - Launch Android Studio.  
   - Select "Open an existing project" and choose the `financepal` folder.  

3. **Build the Project**:  
   - Click on `Build > Rebuild Project` to sync dependencies and build the project.  

4. **Run the Project**:  
   - Connect a device or start an emulator.  
   - Click on the `Run` button or select `Run > Run 'app'` in the menu.  

---

## 🤖 Usage  

1. **Add Entry**:  
   - Open the app and click on "Add Entry."  
   - Fill in the entry details, including optional image attachment and category selection.  

2. **Swipe to Delete & Undo**:  
   - Swipe left on an entry to delete it.  
   - Use the Undo button to restore the deleted entry.  

3. **Scroll Long Descriptions**:  
   - Tap an entry with a long description to view the full content in a scrollable section.  

4. **Filter Entries**:  
   - Navigate between categories like "Sent" or "Received" to view relevant entries.  

---

## 🔧 Technologies Used  

- **Kotlin**: Language used for app development. 🚀  
- **Room Database**: Abstraction over SQLite for reliable and efficient local storage. 🗂️  
- **MVVM Architecture**: Separates UI and logic using ViewModel, Repository, and LiveData. 📊  
- **Material Design Components**: Ensures a visually consistent and modern UI. 🎨  
- **Swipe-to-Delete**: Implemented using ItemTouchHelper for RecyclerView. ↩️  
- **RecyclerView with Custom Adapter**: Displays entries with flexible interaction options. 🔌  
- **Android Jetpack Components**: Includes ViewModel, LiveData, and Room. 🧰  

---

## 🙌 Contributing  

Contributions are welcome! 🤝 Please fork this repository, make your changes, and submit a pull request.  

### Steps to Contribute:  

1. Fork this repository. 🍴  
2. Create a new branch (`git checkout -b feature-branch`). 🌱  
3. Make your changes and commit them (`git commit -m 'Add feature'`). 💻  
4. Push to the branch (`git push origin feature-branch`). 🚀  
5. Open a Pull Request. 🔍  

---  
   