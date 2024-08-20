import com.example.pass_manager.domain.MongoPass
import com.example.pass_manager.domain.MongoPassType
import org.bson.types.ObjectId
import java.math.BigDecimal
import java.time.Instant

object PassFixture {
    private val passTypes = listOf("First type", "Second type", "Third type")
        .map {
            MongoPassType(
                id = ObjectId.get(),
                activeFrom = Instant.MIN,
                activeTo = Instant.MAX,
                name = it,
                price = BigDecimal.TEN
            )
        }

    val passes = passTypes.map {
        MongoPass(
            id = ObjectId.get(),
            purchasedFor = BigDecimal.TEN,
            client = ClientFixture.clientFromDb,
            passType = it,
            purchasedAt = Instant.now(),
        )
    }

    val singlePass = passes.first()
    val singlePassId: ObjectId = singlePass.id!!
}

