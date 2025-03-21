package com.youfeng.sfsmod.ui.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview

import com.youfeng.sfsmod.R

import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer

@Composable
fun CreditsDialog(onDismissRequest: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.osl)) },
        text = { LibrariesContainer(modifier = Modifier.fillMaxSize()) },
        confirmButton = {
            TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.close)) }
        }
    )
}

@Preview
@Composable
fun CreditsDialogPreview() {
    CreditsDialog{}
}
