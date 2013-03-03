package ch.hauth.youknow.ri;

import static ch.hauth.util.data.Sequence.emptyIterable;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMock;
import org.junit.Test;

import ch.hauth.youknow.math.vector.Vector;
import ch.hauth.youknow.math.vector.VectorValue;
import ch.hauth.youknow.ri.content.IHaveContent;
import ch.hauth.youknow.ri.content.IHaveContentWithId;

public class RandomIndexTest {

	@SuppressWarnings("unchecked")
	@Test public void testAddDocumentsSingleWord() {
		final String randomIndexName = "testDocuments";

		IDescribeARandomIndex indexDescription = new IDescribeARandomIndex() {
			@Override
			public String getWordContextSource() {
				return "testMessages";
			}

			@Override
			public String getDocumentSource() {
				return randomIndexName;
			}

			@Override
			public Iterable<? extends IHaveContent> getContentsForWordContext() {
				return emptyIterable();
			}

			@Override
			public Iterable<? extends IHaveContentWithId> getContentsForDocuments() {
				List<IHaveContentWithId> contents = new ArrayList<IHaveContentWithId>();
				TestContent content = new TestContent("doc1");
				content.addContext("test", 20);
				contents.add(content);
				return contents;
			}
		};


		RandomIndexStore store = createMock(RandomIndexStore.class);
		Map<String, Vector> vectors = new HashMap<String, Vector>();
		ArrayList<VectorValue> documentContextValues = new ArrayList<VectorValue>();
		documentContextValues.add(new VectorValue((short) 1, (float) Math.log(20.0d)));
		vectors.put("doc1", Vector.valueOf(documentContextValues));
		store.clearRandomIndex(randomIndexName, 1);
		store.updateRandomIndexClusterMeans(EasyMock.eq(randomIndexName), EasyMock.anyObject(Map.class));
		store.updateRandomIndexCluster(randomIndexName, 0, vectors);
		replay(store);

		WordContext wordContext = createMock(WordContext.class);
		ArrayList<VectorValue> wordContextValues = new ArrayList<VectorValue>();
		wordContextValues.add(new VectorValue((short) 1, 1.0f));
		expect(wordContext.getWordContext("test")).andReturn(Vector.valueOf(wordContextValues)).anyTimes();
		replay(wordContext);

		RandomIndex randomIndex = new RandomIndex(indexDescription, store, wordContext, 1);
		randomIndex.build();
	}
}
