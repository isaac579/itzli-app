package com.eninetworks.notas.ui.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.eninetworks.notas.NotasApp
import com.eninetworks.notas.data.repository.CheckListRepository
import com.eninetworks.notas.data.repository.LabelRepository
import com.eninetworks.notas.data.repository.NoteRepository
import com.eninetworks.notas.data.repository.PasswordRepository
import com.eninetworks.notas.ui.screen.ChangePinScreen
import com.eninetworks.notas.ui.screen.HomeScreen
import com.eninetworks.notas.ui.screen.LockScreen
import com.eninetworks.notas.ui.screen.SearchScreen
import com.eninetworks.notas.ui.screen.SettingsScreen
import com.eninetworks.notas.ui.screen.TrashScreen
import com.eninetworks.notas.ui.screen.WelcomeScreen
import com.eninetworks.notas.ui.screen.lists.ListDetailScreen
import com.eninetworks.notas.ui.screen.lists.ListsScreen
import com.eninetworks.notas.ui.screen.notes.NoteDetailScreen
import com.eninetworks.notas.ui.screen.notes.NotesScreen
import com.eninetworks.notas.ui.screen.passwords.PasswordDetailScreen
import com.eninetworks.notas.ui.screen.passwords.PasswordsScreen
import com.eninetworks.notas.ui.viewmodel.CheckListViewModel
import com.eninetworks.notas.ui.viewmodel.LabelViewModel
import com.eninetworks.notas.ui.viewmodel.LockViewModel
import com.eninetworks.notas.ui.viewmodel.NoteViewModel
import com.eninetworks.notas.ui.viewmodel.PasswordViewModel

private object Routes {
    const val WELCOME         = "welcome"
    const val LOCK            = "lock"
    const val HOME            = "home"
    const val SETTINGS        = "settings"
    const val CHANGE_PIN      = "change_pin"
    const val NOTES           = "notes"
    const val NOTE_DETAIL     = "note_detail/{noteId}"
    const val LISTS           = "lists"
    const val LIST_DETAIL     = "list_detail/{listId}"
    const val PASSWORDS       = "passwords"
    const val PASSWORD_DETAIL = "password_detail/{passwordId}"
    const val SEARCH          = "search"
    const val TRASH           = "trash"
}

@Composable
fun NavGraph() {
    val nav = rememberNavController()
    val ctx = LocalContext.current
    val app = ctx.applicationContext as NotasApp

    val termsAccepted = remember {
        ctx.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .getBoolean("terms_accepted", false)
    }

    val noteFactory  = remember { NoteViewModel.Factory(NoteRepository(app.database.noteDao()), app) }
    val listFactory  = remember { CheckListViewModel.Factory(CheckListRepository(app.database.checkListDao())) }
    val passFactory  = remember { PasswordViewModel.Factory(PasswordRepository(app.database.passwordDao())) }
    val labelFactory = remember { LabelViewModel.Factory(LabelRepository(app.database.labelDao())) }

    val lockVm:  LockViewModel      = viewModel()
    val noteVm:  NoteViewModel      = viewModel(factory = noteFactory)
    val listVm:  CheckListViewModel = viewModel(factory = listFactory)
    val passVm:  PasswordViewModel  = viewModel(factory = passFactory)
    val labelVm: LabelViewModel     = viewModel(factory = labelFactory)

    NavHost(navController = nav, startDestination = if (termsAccepted) Routes.LOCK else Routes.WELCOME) {
        composable(Routes.WELCOME) {
            WelcomeScreen { nav.navigate(Routes.LOCK) { popUpTo(Routes.WELCOME) { inclusive = true } } }
        }
        composable(Routes.LOCK) {
            LockScreen(lockVm) { nav.navigate(Routes.HOME) { popUpTo(Routes.LOCK) { inclusive = true } } }
        }
        composable(Routes.HOME) {
            HomeScreen(
                onNotesClick     = { nav.navigate(Routes.NOTES) },
                onListsClick     = { nav.navigate(Routes.LISTS) },
                onPasswordsClick = { nav.navigate(Routes.PASSWORDS) },
                onSettingsClick  = { nav.navigate(Routes.SETTINGS) },
                onSearchClick    = { nav.navigate(Routes.SEARCH) },
                onTrashClick     = { nav.navigate(Routes.TRASH) }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onChangePinClick = { nav.navigate(Routes.CHANGE_PIN) }, onBack = { nav.popBackStack() })
        }
        composable(Routes.CHANGE_PIN) {
            ChangePinScreen(lockVm) { nav.popBackStack() }
        }
        composable(Routes.NOTES) {
            NotesScreen(noteVm, labelVm, { id -> nav.navigate("note_detail/$id") }, { nav.navigate("note_detail/0") }) { nav.popBackStack() }
        }
        composable(Routes.NOTE_DETAIL) { back ->
            NoteDetailScreen(
                noteId        = back.arguments?.getString("noteId")?.toLongOrNull() ?: 0L,
                viewModel     = noteVm,
                lockViewModel = lockVm,
                labelViewModel = labelVm,
                onBack        = { nav.popBackStack() }
            )
        }
        composable(Routes.LISTS) {
            ListsScreen(listVm, { id -> nav.navigate("list_detail/$id") }) { nav.popBackStack() }
        }
        composable(Routes.LIST_DETAIL) { back ->
            ListDetailScreen(back.arguments?.getString("listId")?.toLongOrNull() ?: 0L, listVm) { nav.popBackStack() }
        }
        composable(Routes.PASSWORDS) {
            PasswordsScreen(passVm, { id -> nav.navigate("password_detail/$id") }, { nav.navigate("password_detail/0") }) { nav.popBackStack() }
        }
        composable(Routes.PASSWORD_DETAIL) { back ->
            PasswordDetailScreen(back.arguments?.getString("passwordId")?.toLongOrNull() ?: 0L, passVm) { nav.popBackStack() }
        }
        composable(Routes.SEARCH) {
            SearchScreen(noteVm, listVm, passVm, { id -> nav.navigate("note_detail/$id") }, { id -> nav.navigate("list_detail/$id") }) { nav.popBackStack() }
        }
        composable(Routes.TRASH) {
            TrashScreen(noteVm) { nav.popBackStack() }
        }
    }
}
