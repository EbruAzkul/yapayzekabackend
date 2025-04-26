import numpy as np
import keras
from keras.preprocessing import image
from flask import Flask, request, jsonify
from PIL import Image
import os
import io

app = Flask(__name__)

# Sabitler
MODEL_PATH = 'C:\\Users\HUAWEI\IdeaProjects\yapayzekabackend\src\main\java\com\example\yapayzekabackend\model_api\cnn_rnn_model.h5'
LABELS = labels = ["Cataract", "Diabetic Retinopathy", "Glaucoma", "Normal"]

INPUT_SIZE = (256, 256)

# Modeli yükle
try:
    model = keras.models.load_model(MODEL_PATH)  # compile=False KULLANILMIYOR
    model.summary()
    print("✅ Model başarıyla yüklendi!")
    print("Model input shape:", model.input_shape)
except Exception as e:
    print(f"❌ Model yüklenirken hata: {str(e)}")
    raise

def preprocess_image(file_stream):
    try:
        img_bytes = file_stream.read()
        img = Image.open(io.BytesIO(img_bytes)).convert('RGB')

        img = img.resize(INPUT_SIZE)
        img_array = np.array(img) / 255.0  # Normalizasyon

        print("📷 Orijinal image shape (HxWxC):", img_array.shape)

        # Model (256, 256, 3) bekliyor, batch boyutu ekliyoruz: (1, 256, 256, 3)
        img_array = np.expand_dims(img_array, axis=0)

        print("✅ Tekli görsel shape:", img_array.shape)

        return img_array
    except Exception as e:
        print("❌ Görüntü işleme hatası:", str(e))
        raise



# Ana tahmin endpoint'i
@app.route('/predict', methods=['POST'])
def predict():
    if 'file' not in request.files:
        return jsonify({'error': 'Lütfen bir görüntü dosyası yükleyin'}), 400

    try:
        file = request.files['file']
        processed_img = preprocess_image(file.stream)

        print("🔍 Model input shape beklenen:", model.input_shape)
        print("📦 Gönderilen veri shape:", processed_img.shape)

        predictions = model.predict(processed_img)[0]
        predicted_class = LABELS[np.argmax(predictions)]
        confidence = float(np.max(predictions))

        print(f"📊 Tahmin sonuçları: {dict(zip(LABELS, predictions))}")

        return jsonify({
            'predicted_class': predicted_class,
            'confidence': confidence,
            'all_probabilities': {lab: float(prob) for lab, prob in zip(LABELS, predictions)}
        })

    except Exception as e:
        import traceback
        traceback.print_exc()
        return jsonify({'error': str(e), 'trace': traceback.format_exc()}), 500

# Test görseliyle model testi
if __name__ == '__main__':
    # Eğer test.jpg varsa, otomatik test et
    if os.path.exists('test.jpg'):
        print("🧪 Test görüntüsü bulundu! Test yapılıyor...")
        with open('test.jpg', 'rb') as f:
            test_img = preprocess_image(f)
            test_pred = model.predict(test_img)[0]
            print("📊 Test Görseli Tahmini:", dict(zip(LABELS, test_pred)))
    else:
        print("⚠️ 'test.jpg' bulunamadı, sadece API başlatılacak.")

    app.run(host='0.0.0.0', port=5001, debug=True)