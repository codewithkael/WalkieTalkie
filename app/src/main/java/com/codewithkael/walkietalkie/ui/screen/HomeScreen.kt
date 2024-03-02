import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.codewithkael.walkietalkie.R
import com.codewithkael.walkietalkie.utils.Constants.CLIENT_SCREEN
import com.codewithkael.walkietalkie.utils.Constants.SERVER_SCREEN

@Composable
fun HomeScreen(navController: NavHostController) {
    val context = LocalContext.current
    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {

        // Title
        Text(
            text = "Welcome to Walkie Talkie",
            style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(16.dp)
        )

        // Welcome Image
        Image(
            painter = painterResource(id = R.drawable.ic_youtube),
            contentDescription = "Walkie Talkie",
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
        )
        ClickableText(
            text = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        color = Color.Blue, fontSize = 22.sp
                    )
                ) {
                    append("Follow codewithkael on YouTube")
                }
            },
            onClick = {
                val uri = Uri.parse("https://www.youtube.com/@codewithkael")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                context.startActivity(intent)
            },
            modifier = Modifier.padding(4.dp),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
        )


        // Server Button with Description
        Button(
            onClick = { navController.navigate(SERVER_SCREEN) },
            modifier = Modifier
                .padding(16.dp)
                .width(200.dp)
        ) {
            Text(
                text = "Start as Server", style = TextStyle(fontSize = 16.sp)
            )
        }

        // Client Button with Description
        Button(
            onClick = { navController.navigate(CLIENT_SCREEN) },
            modifier = Modifier
                .padding(16.dp)
                .width(200.dp)
        ) {
            Text(
                text = "Start as Client", style = TextStyle(fontSize = 16.sp)
            )
        }
    }
}
