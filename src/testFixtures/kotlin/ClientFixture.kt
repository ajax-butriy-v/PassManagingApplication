import com.example.pass_manager.domain.Client
import com.example.pass_manager.domain.MongoPass
import org.bson.types.ObjectId
import java.math.BigDecimal
import java.time.Instant

object ClientFixture {
    val clientId: ObjectId = ObjectId.get()
    val clientToCreate = Client(
        id = null,
        firstName = "First Name",
        lastName = "Last Name",
        phoneNumber = "+123456789",
        email = "example@gmail.com",
        ownedPasses = listOf(
            MongoPass(
                id = PassFixture.singlePassId,
                purchasedFor = BigDecimal.TEN,
                client = null,
                passType = null,
                purchasedAt = Instant.MAX
            )
        )
    )
    val clientFromDb = clientToCreate.copy(id = clientId)
}

