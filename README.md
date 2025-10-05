
# Phonebook App

This project is a phonebook application that allows users to add, edit, delete, and search contacts. The app is written in **Kotlin**, and uses modern Android tools such as **Jetpack Compose** and **Retrofit**.

## Features

- **Add Contact**: Users can add personal details (first name, last name, phone number).
- **Edit Contact**: Users can edit an existing contact.
- **Delete Contact**: Users can delete a contact.
- **Search**: Users can search contacts.
- **Add Contact Image**: Users can add or change a contact’s photo.
- **Save to Phone Contact**: Users can save a contacts to their phone contacts.

## Technologies

- **Kotlin**
- **Jetpack Compose** (UI framework)
- **Retrofit** (API communication)
- **Room Database** (Data storage)
- **Lottie** (Animations)
- **Coil** (Image loading and displaying)

## Backend

Backend operations are performed through a **REST API** using **Retrofit** and **OkHttp**. The API handles **GET**, **POST**, **PUT**, and **DELETE** HTTP methods for data communication. The core API functions are:

- **GET /User/GetAll**: Fetches all contacts.
- **POST /User**: Adds a new contact.
- **PUT /UpdateContact**: Updates an existing contact.
- **DELETE /DeleteContact**: Deletes a contact.

The backend operates under the following parameters:
- **API URL**: `http://146.59.52.68:11235/api/`
- **API Key**: Must be included in each request.

The **RetrofitClient** and **PhonebookService** classes are used to interact with the backend API and perform all operations.

## Frontend

The frontend is built with **Jetpack Compose**, providing a dynamic and modern UI for users. The user interface consists of the following main screens:

1. **ContactsScreen**: Displays the list of contacts.
2. **AddContactScreen**: Screen for adding a new contact.
3. **ProfileScreen**: Displays the profile of a contact with options to view and edit.
4. **SwipeRow**: A component allowing swipe actions to edit or delete contacts.
5. **SuccessMessagePopup**: Displays a popup when an operation is successful.

### UI Features

- **Contact Photo**: Users can add or change a contact’s photo.
- **Search**: Users can search contacts by name, surname, or phone number.
- **Swipe Actions**: Users can swipe a contact to show edit or delete options.
- **Animations**: Lottie animations are used to show success after an operation.

## Setup

1. **Download Project Files**:
   - Download or clone the project from GitHub or any other source.

2. **Install Dependencies**:
   - Open the project in **Android Studio** and sync the Gradle files to install the necessary dependencies.
   
   ```bash
   gradle sync
   ```

3. **Backend API**:
   - Ensure that the backend is running. You can either run it locally or use an existing backend API.

4. **Run the App**:
   - Run the app on an Android device or emulator.
