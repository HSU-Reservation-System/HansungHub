package com.winterflw.hansunghub.reservation

import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.winterflw.hansunghub.reservation.model.FacilityType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationDetailPreviewScreen(
    facility: FacilityType,
    onBackClick: () -> Unit,
    onReserveClick: (FacilityType) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(facility.displayName) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(top = 68.dp)
                .padding(bottom = paddingValues.calculateBottomPadding())
                .verticalScroll(state = rememberScrollState())
                .padding(horizontal = 5.dp),
            verticalArrangement = Arrangement.spacedBy(space = 10.dp)
        ) {
            Image(
                painter = painterResource(id = facility.imgResId),
                contentDescription = facility.displayName,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                contentScale = ContentScale.Crop
            )

            Text(
                text = "시설 상세 정보",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = facility.detaildescription,
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { onReserveClick(facility) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("예약하기")
            }
        }
    }
}
